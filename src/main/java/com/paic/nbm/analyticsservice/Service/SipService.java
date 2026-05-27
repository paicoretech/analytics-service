package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.SipData;
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
public class SipService {

    @Autowired
    EntityManager entityManager;

    public List<String> getGroupingCondition(String startDate, String endDate, String timezone, String ipSrc, String ipDst, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cqSipData = cb.createQuery(String.class);
        Root<SipData> query = cqSipData.from(SipData.class);

        List<Predicate> filterList = new ArrayList<>();

        filterList.add(cb.between(query.get("time_epoch"), epochStartDate, epochEndDate));
        filterList.add(cb.equal(query.get("method"), "INVITE"));
        if (!msisdn.isEmpty()) {
            filterList.add(cb.or(query.get("from_user").in(msisdn), query.get("to_user").in(msisdn)));
        }

        Predicate filter = cb.and(filterList.toArray(Predicate[]::new));

        cqSipData.select(query.get("call_id")).distinct(true);
        cqSipData.where(filter);

        return entityManager.createQuery(cqSipData).getResultList();
    }

    public List<SipData> getData(HashMap<String, Object> filters, String timezone, int totalPerPage, int offSet) {
        // returning an empty list when the call_id-list is not present in the filters
            if (filters.get("call_id-list") == null)
            return new ArrayList<SipData>();
        HashMap<String, Object> filters_final = new HashMap<>(filters);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SipData> query = cb.createQuery(SipData.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<SipData> sipDataRoot = query.from(SipData.class);
        List<Predicate> listFilter = new ArrayList<>();
        Predicate dateFilter =  cb.between(sipDataRoot.get("time_epoch"), epochStartDate, epochEndDate);

        for (String key: filters_final.keySet()) {
            //The filter is a list or a custom filter
            if (key.split("-").length >= 2) {
                String[] data_key = key.split("-");
                switch (data_key[1]){
                    case "list":
                        Expression<String> exp = sipDataRoot.get(data_key[0]);
                        switch (data_key[0]) {
                            case "call_id":
                                List<String> listCallId = (List<String>) filters_final.get(key);
                                listFilter.add(exp.in(listCallId));
                                break;
                        }
                        break;
                }
            }
        }

        Predicate filterPredicate = null;
        if (!listFilter.isEmpty())
            filterPredicate = cb.and(dateFilter, cb.or(listFilter.toArray(Predicate[]::new)));
        else
            filterPredicate = dateFilter;
        query.where(filterPredicate);

        if (totalPerPage > 0) {
            query.orderBy(
                    cb.asc(sipDataRoot.get("time_epoch")),
                    cb.asc(sipDataRoot.get("u_seconds_epoch")));
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

    public List<SipData> getSequenceDiagramQuery(HashMap<String, Object> filters_final, String timezone, int totalPerPage, int offSet) {
        List<SipData> result =  getData(filters_final, timezone, totalPerPage, offSet);
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
        return  result;
    }
}