package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.ProtocolViewData;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._ipNames;

@Service
@RequiredArgsConstructor
public class ProtocolViewService {

    @Autowired
    EntityManager entityManager;

    private int getCountData(List<String> protocols,
                             BigInteger startDate,
                             BigInteger endDate,
                             String msisdn,
                             List<String> imsiList) {
        Query query =  entityManager
            .createNativeQuery(
                "SELECT COUNT(1) " +
                    "FROM getProtocolsData(:protocols, :startDate, :endDate, :msisdn, :imsi) v "
                    )
            .setParameter("protocols", Arrays.deepToString(protocols.toArray()).replace('[', '{').replace(']', '}'))
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .setParameter("msisdn", msisdn)
            .setParameter("imsi", Arrays.deepToString(imsiList.toArray()).replace('[', '{').replace(']', '}'))
        ;
        try {
            var result = query.getResultList();
            return Integer.parseInt(result.get(0).toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private List<ProtocolViewData> getProtocolsData(List<String> protocols,
                                                    BigInteger startDate,
                                                    BigInteger endDate,
                                                    String msisdn,
                                                    List<String> imsi,
                                                    int totalPerPage,
                                                    int offSet) {
        String limitQuery = totalPerPage <=0 ? "" : ("LIMIT " + totalPerPage + " OFFSET " + offSet);
        Query query =  entityManager
            .createNativeQuery(
                "SELECT v.* " +
                    "FROM getProtocolsData(:protocols, :startDate, :endDate, :msisdn, :imsi) v " +
                    limitQuery,
                ProtocolViewData.class)
            .setParameter("protocols", Arrays.deepToString(protocols.toArray()).replace('[', '{').replace(']', '}'))
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .setParameter("msisdn", msisdn)
            .setParameter("imsi", Arrays.deepToString(imsi.toArray()).replace('[', '{').replace(']', '}'))
            ;
        return (List<ProtocolViewData>) query.getResultList();
    }

    public int getSequenceDiagramQueryCount(HashMap<String, Object> filters, HashMap<String, Boolean> protocols, String timezone) {
        return getCount(filters, protocols, 0, 0, timezone);
    }

    public List<ProtocolViewData> getSequenceDiagramQuery(HashMap<String, Object> filters, HashMap<String, Boolean> protocols, int totalPerPage, int offSet, String timezone) {
        List<ProtocolViewData> results = getData(filters, protocols, totalPerPage, offSet, timezone);
            results.forEach(r ->{
                r.setSrc_ip(
                        _ipNames.containsKey(r.getSrc_ip()) ?
                                _ipNames.get((r.getSrc_ip())) :
                                r.getSrc_ip());

                r.setDst_ip(
                        _ipNames.containsKey(r.getDst_ip()) ?
                                _ipNames.get((r.getDst_ip())) :
                                r.getDst_ip());
            });
            return results;
    }



    public List<ProtocolViewData> getData(HashMap<String, Object> filters, HashMap<String, Boolean> protocols, int totalPerPage, int offSet, String timezone) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters.get("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters.get("endDate").toString(), timezone);
        List<String> msisdn = null, imsi = null;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProtocolViewData> query = cb.createQuery(ProtocolViewData.class);
        Root<ProtocolViewData> viewDataRoot = query.from(ProtocolViewData.class);
        List<String> protocolList = new ArrayList<>();
        Predicate finalPredicate = null;
        List<Predicate> filterList = new ArrayList<>();

        Predicate epochFilter = cb.between(viewDataRoot.get("time_epoch"), epochStartDate, epochEndDate);

        if (!filters.containsKey("msisdn") && !filters.containsKey("imsi")) {
            Expression<String> protoExp = viewDataRoot.get("protocol");
            for (Map.Entry<String, Boolean> entry : protocols.entrySet())
                if (entry.getValue())
                    protocolList.add(entry.getKey());
            Predicate protocolFilter = protoExp.in(protocolList);
            finalPredicate = cb.and(epochFilter, protocolFilter);
            query.where(finalPredicate);
        } else if (filters.containsKey("msisdn") || filters.containsKey("imsi")) {
            if (filters.containsKey("msisdn"))
                msisdn = (List<String>) filters.get("msisdn");
            if (filters.containsKey("imsi"))
                imsi = (List<String>) filters.get("imsi");
            Predicate msisdnFilter = msisdn != null ? viewDataRoot.get("msisdn").in(msisdn) : cb.disjunction();
            Predicate imsiFilter = imsi != null ? viewDataRoot.get("imsi").in(imsi) : cb.disjunction();

            for (Map.Entry<String, Boolean> entry : protocols.entrySet()) {
                if (!entry.getValue())
                    continue;
                Predicate protocolFilter = cb.equal(viewDataRoot.get("protocol"), entry.getKey());
                switch (entry.getKey()) {
                    case "CAMEL":
                        if (!filters.containsKey("camel_tcaptid-list"))
                            break;
                        List<BigInteger> camelTcapidList = (List<BigInteger>) filters.get("camel_tcaptid-list");
                        Predicate tidCapFilter = viewDataRoot.get("tcap_tid").in(camelTcapidList);
                        Predicate capFilter = cb.and(protocolFilter, epochFilter, tidCapFilter);

                        filterList.add(capFilter);
                        break;
                    case "DIAMETER":
                        Predicate msisdnImsiDiameterFilter = cb.or(msisdnFilter, imsiFilter);
                        Predicate diameterFilter = cb.and(protocolFilter, epochFilter, msisdnImsiDiameterFilter);
                        filterList.add(diameterFilter);
                        break;
                    case "GTP":
                        Predicate gtpSequenceFilter = cb.conjunction(), gtpTeidFilter = cb.conjunction();
                        if (!filters.containsKey("gtp_teid-list")) {
                            if (!filters.containsKey("gtp_sequence-list"))
                                break;
                            else
                                gtpSequenceFilter = viewDataRoot.get("gtp_seq_number").in((List<BigInteger>) filters.get("gtp_sequence-list"));
                        } else {
                            gtpTeidFilter = viewDataRoot.get("gtp_teid").in((List<BigInteger>) filters.get("gtp_teid-list"));
                            if (filters.containsKey("gtp_sequence-list"))
                                gtpSequenceFilter = viewDataRoot.get("gtp_seq_number").in((List<BigInteger>) filters.get("gtp_sequence-list"));
                        }

                        Predicate msisdnImsiGtpFilter = cb.or(msisdnFilter, imsiFilter);
                        Predicate gtpFilter01 = cb.and(protocolFilter, epochFilter, msisdnImsiGtpFilter, gtpTeidFilter);
                        Predicate gtpFilter02 = cb.and(protocolFilter, epochFilter, msisdnImsiGtpFilter, gtpSequenceFilter);

                        filterList.add(gtpFilter01);
                        filterList.add(gtpFilter02);
                        break;
                    case "HTTP":
                        List<BigInteger> httpIdList = null;
                        Predicate idHttpFilter = cb.disjunction();
                        if (filters.containsKey("http_response_in-list")) {
                            httpIdList = (List<BigInteger>) filters.get("http_response_in-list");
                            idHttpFilter = viewDataRoot.get("id").in(httpIdList);
                        }
                        Predicate msisdnImsiHttpFilter = null;
                        if (msisdn != null && !msisdn.isEmpty()) {
                            msisdnImsiHttpFilter = cb.or(
                                    viewDataRoot.get("msisdn").in(msisdn),
                                    viewDataRoot.get("smpp_src_addr").in(msisdn),
                                    viewDataRoot.get("smpp_dst_addr").in(msisdn),
                                    imsiFilter, idHttpFilter
                            );
                        } else {
                            msisdnImsiHttpFilter = cb.or(
                                    imsiFilter, idHttpFilter
                            );
                        }
                        Predicate httpFilter = cb.and(protocolFilter, epochFilter, msisdnImsiHttpFilter);
                        filterList.add(httpFilter);
                        break;
                    case "HTTP-OCS":
                        List<BigInteger> httpOcsIdList = null;
                        Predicate idHttpOcsFilter = cb.disjunction();
                        if (filters.containsKey("http_response_in-list-ocs")) {
                            httpOcsIdList = (List<BigInteger>) filters.get("http_response_in-list-ocs");
                            idHttpOcsFilter = viewDataRoot.get("id").in(httpOcsIdList);
                        }
                        Predicate msisdnImsiHttpOcsFilter = null;
                        if (msisdn != null && !msisdn.isEmpty()) {
                            msisdnImsiHttpOcsFilter = cb.or(
                                    viewDataRoot.get("msisdn").in(msisdn),
                                    viewDataRoot.get("called").in(msisdn),
                                    viewDataRoot.get("calling").in(msisdn),
                                    viewDataRoot.get("phone").in(msisdn),
                                    imsiFilter, idHttpOcsFilter
                            );
                        } else {
                            msisdnImsiHttpOcsFilter = cb.or(
                                    imsiFilter, idHttpOcsFilter
                            );
                        }
                        Predicate httpOcsFilter = cb.and(protocolFilter, epochFilter, msisdnImsiHttpOcsFilter);
                        filterList.add(httpOcsFilter);
                        break;
                    case "HTTP-SS7":
                        List<String> httpSS7ImsiList = null;
                        List<BigInteger> httpSS7IdList = null;
                        Predicate httpSS7ImsiFilter = cb.conjunction();
                        Predicate httpSS7IdFilter = cb.disjunction();
                        if (filters.containsKey("http_ss7_imsi-list")) {
                            httpSS7ImsiList = (List<String>) filters.get("http_ss7_imsi-list");
                            httpSS7ImsiFilter = viewDataRoot.get("imsi").in(httpSS7ImsiList);
                            if (filters.containsKey("http_ss7_id-list")) {
                                httpSS7IdList = (List<BigInteger>) filters.get("http_ss7_id-list");
                                httpSS7IdFilter = viewDataRoot.get("id").in(httpSS7IdList);
                            }
                        }
                        Predicate msisdnImsiHttpSS7Filter = null;
                        if (msisdn != null && !msisdn.isEmpty()) {
                            if (imsi != null && !imsi.isEmpty()) {
                                msisdnImsiHttpSS7Filter = cb.or(
                                        viewDataRoot.get("msisdn_dest").in(msisdn),
                                        viewDataRoot.get("msisdn_orig").in(msisdn),
                                        imsiFilter
                                );
                            } else {
                                msisdnImsiHttpSS7Filter = cb.or(
                                        cb.and(
                                                viewDataRoot.get("msisdn_orig").in(msisdn)
                                                , httpSS7ImsiFilter)
                                        , httpSS7IdFilter);
                            }
                        } else {
                            msisdnImsiHttpSS7Filter = imsiFilter;
                        }
                        Predicate httpSS7Filter = cb.and(protocolFilter, epochFilter, msisdnImsiHttpSS7Filter);
                        filterList.add(httpSS7Filter);
                        break;
                    case "MAP":
                        Predicate msisdnImsiMapFilter = null;
                        if (msisdn != null && !msisdn.isEmpty()) {
                            if (imsi != null && !imsi.isEmpty()) {
                                msisdnImsiMapFilter = cb.or(
                                        viewDataRoot.get("msisdn_orig_address").in(msisdn),
                                        viewDataRoot.get("msisdn_dest_address").in(msisdn),
                                        imsiFilter
                                );
                            } else {
                                msisdnImsiMapFilter = cb.or(
                                        viewDataRoot.get("msisdn_orig_address").in(msisdn),
                                        viewDataRoot.get("msisdn_dest_address").in(msisdn)
                                );
                            }
                        } else {
                            msisdnImsiMapFilter = imsiFilter;
                        }
                        Predicate mapFilter = cb.and(protocolFilter, epochFilter, msisdnImsiMapFilter);
                        filterList.add(mapFilter);
                        break;
                    case "SIP":
                        if (!filters.containsKey("call_id-list-sip"))
                            break;
                        List<String> callIdList = (List<String>) filters.get("call_id-list-sip");
                        Predicate callIdFilter = viewDataRoot.get("call_id").in(callIdList);
                        Predicate sipFilter = cb.and(protocolFilter, epochFilter, callIdFilter);
                        filterList.add(sipFilter);
                        break;
                    case "SMPP":
                        if ((msisdn == null || msisdn.isEmpty()) && (imsi != null && !imsi.isEmpty()))
                            break;
                        Predicate msisdnSmppFilter = cb.disjunction();
                        if (msisdn != null && !msisdn.isEmpty()) {
                            msisdnSmppFilter = cb.or(
                                    viewDataRoot.get("source_addr").in(msisdn),
                                    viewDataRoot.get("destination_addr").in(msisdn)
                            );
                        }
                        Predicate smppFilter = cb.and(protocolFilter, epochFilter, msisdnSmppFilter);
                        filterList.add(smppFilter);
                        break;
                    default:
                        break;
                }
            }
            if (!filterList.isEmpty())
                query.where(cb.or(filterList.toArray(Predicate[]::new)));
            else
                return new ArrayList<>();
        }
        query.orderBy(cb.asc(viewDataRoot.get("time_epoch")), cb.asc(viewDataRoot.get("useconds_epoch")));
        return entityManager.createQuery(query).setMaxResults(totalPerPage).setFirstResult(offSet).getResultList();
    }

    public int getCount(HashMap<String, Object> filters, HashMap<String, Boolean> protocols, int totalPerPage, int offSet, String timezone) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters.get("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters.get("endDate").toString(), timezone);
        List<String> msisdn = null, imsi = null;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProtocolViewData> viewDataRoot = query.from(ProtocolViewData.class);
        List<String> protocolList = new ArrayList<>();
        Predicate finalPredicate = null;
        List<Predicate> filterList = new ArrayList<>();

        Predicate epochFilter = cb.between(viewDataRoot.get("time_epoch"), epochStartDate, epochEndDate);

        if (!filters.containsKey("msisdn") && !filters.containsKey("imsi")) {
            Expression<String> protoExp = viewDataRoot.get("protocol");
            for (Map.Entry<String, Boolean> entry : protocols.entrySet())
                if (entry.getValue())
                    protocolList.add(entry.getKey());
            Predicate protocolFilter = protoExp.in(protocolList);
            finalPredicate = cb.and(epochFilter, protocolFilter);
            query.where(finalPredicate);
        } else if (filters.containsKey("msisdn") || filters.containsKey("imsi")) {
            if (filters.containsKey("msisdn"))
                msisdn = (List<String>) filters.get("msisdn");
            if (filters.containsKey("imsi"))
                imsi = (List<String>) filters.get("imsi");
            Predicate msisdnFilter = msisdn != null ? viewDataRoot.get("msisdn").in(msisdn) : cb.disjunction();
            Predicate imsiFilter = imsi != null ? viewDataRoot.get("imsi").in(imsi) : cb.disjunction();
            for (Map.Entry<String, Boolean> entry : protocols.entrySet()) {
                if (!entry.getValue())
                    continue;
                Predicate protocolFilter = cb.equal(viewDataRoot.get("protocol"), entry.getKey());
                switch (entry.getKey()) {
                    case "CAMEL":
                        if (!filters.containsKey("camel_tcaptid-list"))
                            break;
                        List<BigInteger> camelTcapidList = (List<BigInteger>) filters.get("camel_tcaptid-list");
                        Predicate tidCapFilter = viewDataRoot.get("tcap_tid").in(camelTcapidList);
                        Predicate capFilter = cb.and(protocolFilter, epochFilter, tidCapFilter);

                        filterList.add(capFilter);
                        break;
                    case "DIAMETER":
                        Predicate msisdnImsiDiameterFilter = cb.or(msisdnFilter, imsiFilter);
                        Predicate diameterFilter = cb.and(protocolFilter, epochFilter, msisdnImsiDiameterFilter);
                        filterList.add(diameterFilter);
                        break;
                    case "GTP":
                        Predicate gtpSequenceFilter = cb.conjunction(), gtpTeidFilter = cb.conjunction();
                        if (!filters.containsKey("gtp_teid-list")) {
                            if (!filters.containsKey("gtp_sequence-list"))
                                break;
                            else
                                gtpSequenceFilter = viewDataRoot.get("gtp_seq_number").in((List<BigInteger>) filters.get("gtp_sequence-list"));
                        } else {
                            gtpTeidFilter = viewDataRoot.get("gtp_teid").in((List<BigInteger>) filters.get("gtp_teid-list"));
                            if (filters.containsKey("gtp_sequence-list"))
                                gtpSequenceFilter = viewDataRoot.get("gtp_seq_number").in((List<BigInteger>) filters.get("gtp_sequence-list"));
                        }

                        Predicate msisdnImsiGtpFilter = cb.or(msisdnFilter, imsiFilter);
                        Predicate gtpFilter01 = cb.and(protocolFilter, epochFilter, msisdnImsiGtpFilter, gtpTeidFilter);
                        Predicate gtpFilter02 = cb.and(protocolFilter, epochFilter, msisdnImsiGtpFilter, gtpSequenceFilter);

                        filterList.add(gtpFilter01);
                        filterList.add(gtpFilter02);
                        break;
                    case "HTTP":
                        List<BigInteger> httpIdList = null;
                        Predicate idHttpFilter = cb.disjunction();
                        if (filters.containsKey("http_response_in-list")) {
                            httpIdList = (List<BigInteger>) filters.get("http_response_in-list");
                            idHttpFilter = viewDataRoot.get("id").in(httpIdList);
                        }
                        Predicate msisdnImsiHttpFilter = null;
                        if (msisdn != null && !msisdn.isEmpty()) {
                            msisdnImsiHttpFilter = cb.or(
                                    viewDataRoot.get("msisdn").in(msisdn),
                                    viewDataRoot.get("smpp_src_addr").in(msisdn),
                                    viewDataRoot.get("smpp_dst_addr").in(msisdn),
                                    imsiFilter, idHttpFilter
                            );
                        } else {
                            msisdnImsiHttpFilter = cb.or(
                                    imsiFilter, idHttpFilter
                            );
                        }
                        Predicate httpFilter = cb.and(protocolFilter, epochFilter, msisdnImsiHttpFilter);
                        filterList.add(httpFilter);
                        break;
                    case "HTTP-OCS":
                        List<BigInteger> httpOcsIdList = null;
                        Predicate idHttpOcsFilter = cb.disjunction();
                        if (filters.containsKey("http_response_in-list-ocs")) {
                            httpOcsIdList = (List<BigInteger>) filters.get("http_response_in-list-ocs");
                            idHttpOcsFilter = viewDataRoot.get("id").in(httpOcsIdList);
                        }
                        Predicate msisdnImsiHttpOcsFilter = null;
                        if (msisdn != null && !msisdn.isEmpty()) {
                            msisdnImsiHttpOcsFilter = cb.or(
                                    viewDataRoot.get("msisdn").in(msisdn),
                                    viewDataRoot.get("called").in(msisdn),
                                    viewDataRoot.get("calling").in(msisdn),
                                    viewDataRoot.get("phone").in(msisdn),
                                    imsiFilter, idHttpOcsFilter
                            );
                        } else {
                            msisdnImsiHttpOcsFilter = cb.or(
                                    imsiFilter, idHttpOcsFilter
                            );
                        }
                        Predicate httpOcsFilter = cb.and(protocolFilter, epochFilter, msisdnImsiHttpOcsFilter);
                        filterList.add(httpOcsFilter);
                        break;
                    case "HTTP-SS7":
                        List<String> httpSS7ImsiList = null;
                        List<BigInteger> httpSS7IdList = null;
                        Predicate httpSS7ImsiFilter = cb.conjunction();
                        Predicate httpSS7IdFilter = cb.disjunction();
                        if (filters.containsKey("http_ss7_imsi-list")) {
                            httpSS7ImsiList = (List<String>) filters.get("http_ss7_imsi-list");
                            httpSS7ImsiFilter = viewDataRoot.get("imsi").in(httpSS7ImsiList);
                            if (filters.containsKey("http_ss7_id-list")) {
                                httpSS7IdList = (List<BigInteger>) filters.get("http_ss7_id-list");
                                httpSS7IdFilter = viewDataRoot.get("id").in(httpSS7IdList);
                            }
                        }
                        Predicate msisdnImsiHttpSS7Filter = null;
                        if (msisdn != null && !msisdn.isEmpty()) {
                            if (imsi != null && !imsi.isEmpty()) {
                                msisdnImsiHttpSS7Filter = cb.or(
                                        viewDataRoot.get("msisdn_dest").in(msisdn),
                                        viewDataRoot.get("msisdn_orig").in(msisdn),
                                        imsiFilter
                                );
                            } else {
                                msisdnImsiHttpSS7Filter = cb.or(
                                        cb.and(
                                                viewDataRoot.get("msisdn_orig").in(msisdn)
                                                , httpSS7ImsiFilter)
                                        , httpSS7IdFilter);
                            }
                        } else {
                            msisdnImsiHttpSS7Filter = imsiFilter;
                        }
                        Predicate httpSS7Filter = cb.and(protocolFilter, epochFilter, msisdnImsiHttpSS7Filter);
                        filterList.add(httpSS7Filter);
                        break;
                    case "MAP":
                        Predicate msisdnImsiMapFilter = null;
                        if (msisdn != null && !msisdn.isEmpty()) {
                            if (imsi != null && !imsi.isEmpty()) {
                                msisdnImsiMapFilter = cb.or(
                                        viewDataRoot.get("msisdn_orig_address").in(msisdn),
                                        viewDataRoot.get("msisdn_dest_address").in(msisdn),
                                        imsiFilter
                                );
                            } else {
                                msisdnImsiMapFilter = cb.or(
                                        viewDataRoot.get("msisdn_orig_address").in(msisdn),
                                        viewDataRoot.get("msisdn_dest_address").in(msisdn)
                                );
                            }
                        } else {
                            msisdnImsiMapFilter = imsiFilter;
                        }
                        Predicate mapFilter = cb.and(protocolFilter, epochFilter, msisdnImsiMapFilter);
                        filterList.add(mapFilter);
                        break;
                    case "SIP":
                        if (!filters.containsKey("call_id-list-sip"))
                            break;
                        List<String> callIdList = (List<String>) filters.get("call_id-list-sip");
                        Predicate callIdFilter = viewDataRoot.get("call_id").in(callIdList);
                        Predicate sipFilter = cb.and(protocolFilter, epochFilter, callIdFilter);
                        filterList.add(sipFilter);
                        break;
                    case "SMPP":
                        if ((msisdn == null || msisdn.isEmpty()) && (imsi != null && !imsi.isEmpty()))
                            break;
                        Predicate msisdnSmppFilter = cb.disjunction();
                        if (msisdn != null && !msisdn.isEmpty()) {
                            msisdnSmppFilter = cb.or(
                                    viewDataRoot.get("source_addr").in(msisdn),
                                    viewDataRoot.get("destination_addr").in(msisdn)
                            );
                        }
                        Predicate smppFilter = cb.and(protocolFilter, epochFilter, msisdnSmppFilter);
                        filterList.add(smppFilter);
                        break;
                    default:
                        break;
                }
            }
            if (!filterList.isEmpty())
                query.where(cb.or(filterList.toArray(Predicate[]::new)));
            else
                return 0;
        }
        query.select(cb.count(viewDataRoot));
        long count = entityManager.createQuery(query).getSingleResult();
        return Math.toIntExact(count);
    }

}
