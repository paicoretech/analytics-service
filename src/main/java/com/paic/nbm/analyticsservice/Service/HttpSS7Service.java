package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.HttpSS7Data;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._ipNames;

@Service
@RequiredArgsConstructor
public class HttpSS7Service {

    @Autowired
    EntityManager entityManager;

    @Value("${seconds.diff.trans}")
    BigInteger secondsDifferenceBetweenTransactions;

    public List<String> getOffnetImsi(String startDate, String endDate, String timezone, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<HttpSS7Data> query = cq.from(HttpSS7Data.class);

        // Offnet scenario, trying to get the IMSI from the sriForSm request
        // Reminder: the IMSI is being copied between the request and responses in the ingestion process
        Predicate dateFilter = cb.between(query.get("time_epoch"), epochStartDate, epochEndDate);
        Predicate offnetFilter = cb.and(
                dateFilter, query.get("msisdn_orig").in(msisdn), cb.isTrue(query.get("http_is_request"))
                , cb.equal(query.get("type"), "sriForSm"), cb.isNotNull(query.get("imsi")), cb.notEqual(query.get("imsi"), ""));
        cq.select(query.get("imsi")).distinct(true).where(offnetFilter);
        return entityManager.createQuery(cq).getResultList();
    }

    public List<String> getOffnetMsisdn(String startDate, String endDate, String timezone, List<String> imsi) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<HttpSS7Data> query = cq.from(HttpSS7Data.class);

        // Offnet scenario, trying to get the MSISDN Originating from the sriForSm request
        // Reminder: the MSISDN Originating is being copied between the request and responses in the ingestion process
        Predicate dateFilter = cb.between(query.get("time_epoch"), epochStartDate, epochEndDate);
        Predicate offnetFilter = cb.and(
                dateFilter, query.get("imsi").in(imsi), cb.isTrue(query.get("http_is_request"))
                , cb.equal(query.get("type"), "sriForSm"), cb.isNotNull(query.get("msisdn_orig")), cb.notEqual(query.get("msisdn_orig"), ""));
        cq.select(query.get("msisdn_orig")).distinct(true).where(offnetFilter);
        return entityManager.createQuery(cq).getResultList();
    }

    public List<HttpSS7Data> getGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<HttpSS7Data> cq = cb.createQuery(HttpSS7Data.class);
        Root<HttpSS7Data> query = cq.from(HttpSS7Data.class);

        List<Predicate> imsiMsisdnFilter = new ArrayList<>();
        Predicate dateFilter = cb.between(query.get("time_epoch"), epochStartDate, epochEndDate);
        Predicate finalPredicate;

        if (!msisdn.isEmpty()) {
            if (imsi.isEmpty()) {
                imsiMsisdnFilter.add(query.get("msisdn_orig").in(msisdn));
                imsiMsisdnFilter.add(query.get("msisdn_dest").in(msisdn));
                finalPredicate = cb.and(dateFilter, cb.or(imsiMsisdnFilter.toArray(Predicate[]::new)), cb.isTrue(query.get("http_is_request")), cb.equal(query.get("type"), "smsmt"));
                cq.multiselect(query.get("time_epoch"), query.get("imsi")).where(finalPredicate);
                return entityManager.createQuery(cq).getResultList();
            }
        }
        cq.multiselect(query.get("imsi")).where(cb.equal(cb.literal(1), cb.literal(0)));
        return entityManager.createQuery(cq).getResultList();
    }

    public List<HttpSS7Data> getGroupingCondition2(List<HttpSS7Data> httpSS7DataList) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<HttpSS7Data> cq = cb.createQuery(HttpSS7Data.class);
        Root<HttpSS7Data> query = cq.from(HttpSS7Data.class);
        Predicate finalPredicate;
        List<HttpSS7Data> idList = new ArrayList<>();
        for (HttpSS7Data httpSS7Data : httpSS7DataList) {
            finalPredicate = cb.and(
                    cb.between(
                            query.get("time_epoch")
                            , httpSS7Data.getTime_epoch().subtract(secondsDifferenceBetweenTransactions)
                            , httpSS7Data.getTime_epoch())
                    , cb.equal(query.get("imsi"), httpSS7Data.getImsi())
                    , cb.isTrue(query.get("http_is_request"))
                    , cb.equal(query.get("type"), "sriForSm"));
            cq.multiselect(query.get("id"), query.get("http_response_in")).where(finalPredicate);
            idList.addAll(entityManager.createQuery(cq).getResultList());
        }
        return idList;
    }

    public List<HttpSS7Data> getData(HashMap<String, Object> filters, String timezone, int totalPerPage, int offSet) {
        // returning an empty list when the http_response_in-list is not present in the filters
        if (filters.get("http_response_in-list") == null)
            return new ArrayList<HttpSS7Data>();
        HashMap<String, Object> filters_final = new HashMap<>(filters);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<HttpSS7Data> query = cb.createQuery(HttpSS7Data.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<HttpSS7Data> httpDataRoot = query.from(HttpSS7Data.class);
        List<Predicate> listFilter = new ArrayList<>();
        List<Predicate> listMsisdnImsiFilter = new ArrayList<>();
        Predicate dateFilter = cb.between(httpDataRoot.get("time_epoch"), epochStartDate, epochEndDate);
        Predicate responsesIdsFilter = null;
        listFilter.add(dateFilter);
        Expression<String> exp;

        if (filters_final.get("msisdn") != null)
            listMsisdnImsiFilter.add(httpDataRoot.get("msisdn_dest").in(Collections.singletonList(filters_final.get("msisdn"))));

        if (filters_final.get("imsi") != null)
            listMsisdnImsiFilter.add(httpDataRoot.get("imsi").in(Collections.singletonList(filters_final.get("imsi"))));

        if (filters_final.get("http_response_in-list") != null) {
            exp = httpDataRoot.get("id");
            responsesIdsFilter = exp.in((List<BigInteger>) filters_final.get("http_response_in-list"));
        }

        Predicate filterPredicate = null;
        if (listMsisdnImsiFilter.isEmpty()) {
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

    public List<HttpSS7Data> getSequenceDiagramQuery(HashMap<String, Object> filters_final, String timezone, int totalPerPage, int offSet) {
        List<HttpSS7Data> result = getData(filters_final, timezone, totalPerPage, offSet);
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

    public List<HttpSS7Data> getSequenceDiagram(List<HttpSS7Data> dataList) {
        dataList.forEach(ss7Data -> {
            ss7Data.setSrc_ip(
                    _ipNames.containsKey(ss7Data.getSrc_ip()) ?
                            _ipNames.get((ss7Data.getSrc_ip())) :
                            ss7Data.getSrc_ip());

            ss7Data.setDst_ip(
                    _ipNames.containsKey(ss7Data.getDst_ip()) ?
                            _ipNames.get((ss7Data.getDst_ip())) :
                            ss7Data.getDst_ip());
        });
        return dataList;
    }
}
