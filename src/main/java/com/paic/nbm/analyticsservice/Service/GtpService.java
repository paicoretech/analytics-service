package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.GtpData;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._ipNames;

@Service
@RequiredArgsConstructor
public class GtpService {

    @Autowired
    EntityManager entityManager;

    public List<BigInteger> getGroupingConditionTunnelList(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigInteger> cq = cb.createQuery(BigInteger.class);
        Root<GtpData> query = cq.from(GtpData.class);
        List<Predicate> filter = new ArrayList<>();
        List<Predicate> imsiMsisdnFilter = new ArrayList<>();
        Predicate dateFilter =  cb.between(query.get("time_epoch"), epochStartDate, epochEndDate);
        filter.add(dateFilter);

        if (!imsi.isEmpty())
            imsiMsisdnFilter.add(query.get("imsi").in(imsi));
        if (!msisdn.isEmpty())
            imsiMsisdnFilter.add(query.get("msisdn").in(msisdn));
        if (!imsiMsisdnFilter.isEmpty())
            filter.add(cb.or(imsiMsisdnFilter.toArray(Predicate[]::new)));

        filter.add(cb.notEqual(query.get("gtp_teid"), 0));

        Predicate finalPredicate = cb.and(filter.toArray(Predicate[]::new));
        cq.select(query.get("gtp_teid")).distinct(true);
        cq.where(finalPredicate);
        return entityManager.createQuery(cq).getResultList();
    }

    public List<BigInteger> getGroupingConditionSequenceList(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigInteger> cq = cb.createQuery(BigInteger.class);
        Root<GtpData> query = cq.from(GtpData.class);
        List<Predicate> filter = new ArrayList<>();
        List<Predicate> imsiMsisdnFilter = new ArrayList<>();
        Predicate dateFilter =  cb.between(query.get("time_epoch"), epochStartDate, epochEndDate);
        filter.add(dateFilter);

        if (!imsi.isEmpty())
            imsiMsisdnFilter.add(query.get("imsi").in(imsi));
        if (!msisdn.isEmpty())
            imsiMsisdnFilter.add(query.get("msisdn").in(msisdn));
        if (!imsiMsisdnFilter.isEmpty())
            filter.add(cb.or(imsiMsisdnFilter.toArray(Predicate[]::new)));

        filter.add(cb.equal(query.get("gtp_teid"), 0));
        filter.add(cb.equal(query.get("gtp_message"), "Create Session Request"));

        Predicate finalPredicate = cb.and(filter.toArray(Predicate[]::new));
        cq.select(query.get("gtp_seq_number")).distinct(true);
        cq.where(finalPredicate);
        return entityManager.createQuery(cq).getResultList();
    }

    public List<GtpData> getData(HashMap<String, Object> filters, String timezone, int totalPerPage, int offSet) {
        HashMap<String, Object> filters_final = new HashMap<>(filters);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GtpData> query = cb.createQuery(GtpData.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<GtpData> gtpDataRoot = query.from(GtpData.class);
        List<Predicate> listFilter = new ArrayList<>();
        List<Predicate> imsiMsisdnFilter = new ArrayList<>();
        List<Predicate> listCustomFilterOr = new ArrayList<>();
        List<Predicate> listCustomFilterAnd = new ArrayList<>();
        Predicate dateFilter =  cb.between(gtpDataRoot.get("time_epoch"), epochStartDate, epochEndDate);
        listFilter.add(dateFilter);

        for (String key: filters_final.keySet()) {
            //The filter is a list or a custom filter
            if (key.split("-").length >= 2) {
                String[] data_key = key.split("-");
                switch (data_key[1]){
                    case "list":
                        Expression<String> exp = gtpDataRoot.get(data_key[0]);
                        Expression<BigInteger> expBigInteger = gtpDataRoot.get(data_key[0]);
                        switch (data_key[0]) {
                            case "gtp_message":
                                List<String> list_gtp_message = (List<String>) filters_final.get(key);
                                listFilter.add(exp.in(list_gtp_message));
                                break;

                            case "gtp_seq_number":
                                List<BigInteger> list_gtp_seq_number = (List<BigInteger>) filters_final.get(key);
                                listFilter.add(expBigInteger.in(list_gtp_seq_number));
                                break;

                        }
                        break;

                    case "custom":
                        HashMap<String,Object> customFilter = (HashMap<String, Object>) filters_final.get(key);
                        String filter = customFilter.get("filter").toString();
                        String value = customFilter.get("value").toString();
                        String type = customFilter.get("type").toString();
                        if (filter.equals("AND")) {
                            switch (type) {
                                case "String":
                                    listCustomFilterAnd.add(cb.like(gtpDataRoot.get(data_key[0]), value));
                                    break;
                            }

                        } else {
                            switch (type) {
                                case "String":
                                    listCustomFilterOr.add(cb.like(gtpDataRoot.get(data_key[0]), value));
                                    break;
                            }

                        }
                        break;
                }
            } else {
                Object value = filters_final.get(key);
                if (value.toString().isBlank() || value.toString().equals("NA") || value.toString().equals("[]"))
                    continue;

                if ("msisdn".equals(key) || "imsi".equals(key))
                    imsiMsisdnFilter.add(gtpDataRoot.get(key).in(value));
                else
                    listFilter.add(cb.like(gtpDataRoot.get(key), value.toString()));
            }
        }

        if (!imsiMsisdnFilter.isEmpty())
            listFilter.add(cb.or(imsiMsisdnFilter.toArray(Predicate[]::new)));

        Predicate filterPredicate = cb.and(listFilter.toArray(Predicate[]::new));
        Predicate filterOrPredicate = cb.or(listCustomFilterOr.toArray(Predicate[]::new));
        Predicate filterAndPredicate = cb.and(listCustomFilterAnd.toArray(Predicate[]::new));
        if (listCustomFilterOr.size() > 0) {
            query.where(filterPredicate, filterOrPredicate);
        } else {
            query.where(filterPredicate, filterAndPredicate);
        }

        if (totalPerPage > 0) {
            return entityManager.createQuery(query)
                    .setMaxResults(totalPerPage)
                    .setFirstResult(offSet)
                    .getResultList();
        } else {
            return entityManager.createQuery(query).getResultList();
        }
    }

    public int getSequenceDiagramQueryCount(HashMap<String, Object> filters, String timezone) {
        return getData(filters, timezone, 0, 0).size();
    }

    public List<GtpData> getSequenceDiagramQuery(HashMap<String, Object> filters_final, String timezone, int totalPerPage, int offSet) {
        List<GtpData> result = getData(filters_final, timezone, totalPerPage, offSet);
        result.forEach(r -> {
            r.setReal_src_ip(r.getSrc_ip());
            r.setReal_dst_ip(r.getDst_ip());
            r.setSrc_ip(
                    _ipNames.containsKey(r.getSrc_ip()) ?
                            _ipNames.get((r.getSrc_ip())) :
                            r.getSrc_ip());

            r.setDst_ip(
                    _ipNames.containsKey(r.getDst_ip()) ?
                            _ipNames.get((r.getDst_ip())) :
                            r.getDst_ip());
        });
        return result;
    }
}
