package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.CamelData;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CamelService {

    @Autowired
    EntityManager entityManager;

    public List<CamelData> getGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CamelData> cq = cb.createQuery(CamelData.class);
        Root<CamelData> query = cq.from(CamelData.class);
        List<Predicate> filter = new ArrayList<>();
        Predicate dateFilter =  cb.between(query.get("time_epoch"), epochStartDate, epochEndDate);

        if (!msisdn.isEmpty())
            filter.add(query.get("msisdn").in(msisdn));

        if (!imsi.isEmpty())
            filter.add(query.get("imsi").in(imsi));


        Predicate finalPredicate = null;
        if (!filter.isEmpty())
            finalPredicate = cb.and(dateFilter, cb.or(filter.toArray(Predicate[]::new)));
        else
            finalPredicate = dateFilter;

        cq.multiselect(query.get("tcap_tid")).distinct(true);
        cq.where(finalPredicate);
        return entityManager.createQuery(cq).getResultList();
    }

    public int getSequenceDiagramQueryCount(HashMap<String, Object> filters, String timezone) {
        return getData(filters, timezone, 0, 0).size();
    }


    public List<CamelData> getSequenceDiagramQuery(HashMap<String, Object> filters_final, String timezone, int totalPerPage, int offSet) {
        return getData(filters_final, timezone, totalPerPage, offSet);
    }

    public List<CamelData> getData(HashMap<String, Object> filters, String timezone, int totalPerPage, int offSet) {
        if (filters.get("tcap_otid-list") == null && filters.get("tcap_dtid-list") == null)
            return new ArrayList<>();
        HashMap<String, Object> filters_final = new HashMap<>(filters);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CamelData> query = cb.createQuery(CamelData.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<CamelData> camelDataRoot = query.from(CamelData.class);
        List<Predicate> listFilter = new ArrayList<>();
        Predicate dateFilter =  cb.between(camelDataRoot.get("time_epoch"), epochStartDate, epochEndDate);

        for (String key: filters_final.keySet()) {
            //The filter is a list or a custom filter
            if (key.split("-").length >= 2) {
                String[] data_key = key.split("-");
                if ("list".equals(data_key[1])) {
                    Expression<BigInteger> exp = camelDataRoot.get(data_key[0]);
                    switch (data_key[0]) {
                        case "tcap_otid":
                        case "tcap_dtid":
                            List<BigInteger> listString = (List<BigInteger>) filters_final.get(key);
                            listFilter.add(exp.in(listString));
                            break;

                    }
                }
            } else {
                Object value = filters_final.get(key);
                if (value.toString().isBlank() || value.toString().equals("NA") || value.toString().equals("[]"))
                    continue;

                if ("msisdn".equals(key) || "imsi".equals(key))
                    listFilter.add(camelDataRoot.get(key).in(Collections.singletonList(value)));
                else
                    listFilter.add(cb.like(camelDataRoot.get(key), value.toString()));
            }

        }

        Predicate finalPredicate = null;
        if (listFilter.size() > 0)
            finalPredicate = cb.and(dateFilter, cb.or(listFilter.toArray(Predicate[]::new)));
        else
            finalPredicate = dateFilter;

        query.where(finalPredicate);


        if (totalPerPage > 0) {
            return entityManager.createQuery(query)
                    .setMaxResults(totalPerPage)
                    .setFirstResult(offSet)
                    .getResultList();
        } else {
            return entityManager.createQuery(query).getResultList();
        }
    }
}
