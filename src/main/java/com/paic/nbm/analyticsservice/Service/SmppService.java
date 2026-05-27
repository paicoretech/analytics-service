package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.SmppData;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._ipNames;

@Service
@RequiredArgsConstructor
public class SmppService {

    @Autowired
    EntityManager entityManager;


    public List<SmppData> getGroupingCondition(String startDate, String endDate, String timezone, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SmppData> cq = cb.createQuery(SmppData.class);
        Root<SmppData> query = cq.from(SmppData.class);
        List<Predicate> filter = new ArrayList<>();
        Predicate dateFilter = cb.between(query.get("time_epoch"), epochStartDate, epochEndDate);

        if (!msisdn.isEmpty()) {
            filter.add(query.get("source_addr").in(msisdn));
            filter.add(query.get("destination_addr").in(msisdn));
        }

        Predicate finalPredicate = null;
        if (filter.size() > 0)
            finalPredicate = cb.and(dateFilter, cb.or(filter.toArray(Predicate[]::new)));
        else
            finalPredicate = dateFilter;
        cq.multiselect(query.get("src_ip"), query.get("dst_ip"), query.get("sequence_number")).distinct(true);
        cq.where(finalPredicate);
        return entityManager.createQuery(cq).getResultList();
    }

    public List<SmppData> getData(HashMap<String, Object> filters, String timezone, int totalPerPage, int offSet) {
        // returning an empty list when the following filters are not present
        if (filters.get("src_ip-list") == null && filters.get("dst_ip-list") == null &&
                filters.get("sequence_number-list") == null)
            return new ArrayList<SmppData>();
        HashMap<String, Object> filters_final = new HashMap<>(filters);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SmppData> query = cb.createQuery(SmppData.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<SmppData> smppDataRoot = query.from(SmppData.class);
        List<Predicate> listFilter = new ArrayList<>();
        Predicate dateFilter = cb.between(smppDataRoot.get("time_epoch"), epochStartDate, epochEndDate);
        listFilter.add(dateFilter);

        Expression<String> exp = smppDataRoot.get("sequence_number");
        List<Integer> listInt = (List<Integer>) filters_final.get("sequence_number-list");
        listFilter.add(exp.in(listInt));

        // Merging and removing duplicates from next 2 lists to link the requests and responses
        List<String> srcIpList = (List<String>) filters_final.get("src_ip-list");
        List<String> dstIpList = (List<String>) filters_final.get("dst_ip-list");
        List<String> srcIpAndDstIpList = new ArrayList<String>();
        srcIpAndDstIpList.addAll(srcIpList);
        srcIpAndDstIpList.addAll(dstIpList);
        srcIpAndDstIpList = UtilityFunctions.cleanList(srcIpAndDstIpList);

        exp = smppDataRoot.get("src_ip");
        listFilter.add(exp.in(srcIpAndDstIpList));

        exp = smppDataRoot.get("dst_ip");
        listFilter.add(exp.in(srcIpAndDstIpList));

        Predicate filterPredicate = cb.and(listFilter.toArray(Predicate[]::new));
        query.where(filterPredicate);

        if (totalPerPage > 0) {
            query.orderBy(
                    cb.asc(smppDataRoot.get("time_epoch")),
                    cb.asc(smppDataRoot.get("u_seconds_epoch")));
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


    public List<SmppData> getSequenceDiagramQuery(HashMap<String, Object> filters_final, String timezone, int totalPerPage, int offSet) {
        List<SmppData> result = getData(filters_final, timezone, totalPerPage, offSet);
        result.forEach(smppData -> {
            smppData.setReal_src_ip(smppData.getSrc_ip());
            smppData.setReal_dst_ip(smppData.getDst_ip());
            smppData.setSrc_ip(
                    _ipNames.containsKey(smppData.getSrc_ip()) ?
                            _ipNames.get((smppData.getSrc_ip())) :
                            smppData.getSrc_ip());

            smppData.setDst_ip(
                    _ipNames.containsKey(smppData.getDst_ip()) ?
                            _ipNames.get((smppData.getDst_ip())) :
                            smppData.getDst_ip());
        });
        return result;
    }
}
