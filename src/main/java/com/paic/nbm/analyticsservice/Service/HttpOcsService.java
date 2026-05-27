package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.HttpOcsData;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.*;

import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._ipNames;

@Service
@RequiredArgsConstructor
public class HttpOcsService {

    @Autowired
    EntityManager entityManager;


    public List<HttpOcsData> getGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<HttpOcsData> cq = cb.createQuery(HttpOcsData.class);
        Root<HttpOcsData> query = cq.from(HttpOcsData.class);
        List<Predicate> imsiMsisdnFilter = new ArrayList<>();
        Predicate dateFilter =  cb.between(query.get("time_epoch"), epochStartDate, epochEndDate);

        if (!imsi.isEmpty())
            imsiMsisdnFilter.add(query.get("imsi").in(imsi));

        if (!msisdn.isEmpty()) {
            imsiMsisdnFilter.add(query.get("msisdn").in(msisdn));
            imsiMsisdnFilter.add(query.get("phone").in(msisdn));
            imsiMsisdnFilter.add(query.get("called").in(msisdn));
            imsiMsisdnFilter.add(query.get("calling").in(msisdn));
        }

        Predicate finalPredicate = dateFilter;
        if (imsiMsisdnFilter.size() > 0)
            finalPredicate = cb.and(dateFilter, cb.or(imsiMsisdnFilter.toArray(Predicate[]::new)));

        cq.multiselect(query.get("http_response_in")).distinct(true);
        cq.where(finalPredicate);
        return entityManager.createQuery(cq).getResultList();
    }

    public List<HttpOcsData> getData(HashMap<String, Object> filters, String timezone, int totalPerPage, int offSet) {
        // returning an empty list when the http_response_in-list is not present in the filters
        if (filters.get("http_response_in-list") == null)
            return new ArrayList<HttpOcsData>();
        HashMap<String, Object> filters_final = new HashMap<>(filters);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<HttpOcsData> query = cb.createQuery(HttpOcsData.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<HttpOcsData> httpDataRoot = query.from(HttpOcsData.class);
        List<Predicate> listFilter = new ArrayList<>();
        List<Predicate> listMsisdnImsiFilter = new ArrayList<>();
        Predicate dateFilter =  cb.between(httpDataRoot.get("time_epoch"), epochStartDate, epochEndDate);
        Predicate responsesIdsFilter = null;
        listFilter.add(dateFilter);
        Expression<BigInteger> exp;

        if (filters_final.get("msisdn") != null)
            listMsisdnImsiFilter.add(httpDataRoot.get("msisdn").in(Collections.singletonList(filters_final.get("msisdn"))));

        if (filters_final.get("imsi") != null)
            listMsisdnImsiFilter.add(httpDataRoot.get("imsi").in(Collections.singletonList(filters_final.get("imsi"))));

        if (filters_final.get("http_response_in-list") != null) {
            exp = httpDataRoot.get("id");
            responsesIdsFilter = exp.in((List<BigInteger>) filters_final.get("http_response_in-list"));
        }

        Predicate filterPredicate = null;
        if (listMsisdnImsiFilter.size() == 0) {
            if (responsesIdsFilter == null)
                filterPredicate = dateFilter;
            else
                filterPredicate = cb.or(dateFilter, responsesIdsFilter);
        } else {
            if (responsesIdsFilter == null)
                filterPredicate = cb.and(dateFilter, cb.or(listMsisdnImsiFilter.toArray(Predicate[]::new)));
            else
                filterPredicate = cb.or(cb.and(dateFilter, cb.or(listMsisdnImsiFilter.toArray(Predicate[]::new))), responsesIdsFilter);
        }

        query.where(filterPredicate);

        if (totalPerPage > 0) {
            query.orderBy(
                    cb.asc(httpDataRoot.get("time_epoch")),
                    cb.asc(httpDataRoot.get("u_seconds_epoch")));
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

    public List<HttpOcsData> getSequenceDiagramQuery(HashMap<String, Object> filters_final, String timezone, int totalPerPage, int offSet) {
        List<HttpOcsData> result = getData(filters_final, timezone, totalPerPage, offSet);
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
