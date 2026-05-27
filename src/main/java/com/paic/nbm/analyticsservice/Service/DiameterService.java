package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.CommandCodeCatalog;
import com.paic.nbm.analyticsservice.Entities.DiameterData;
import com.paic.nbm.analyticsservice.Entities.DiameterDictionary;
import com.paic.nbm.analyticsservice.Entities.DiameterWatchdogAlarm;
import com.paic.nbm.analyticsservice.Entities.IpNamesData;
import com.paic.nbm.analyticsservice.Entities.PeersDRA;
import com.paic.nbm.analyticsservice.Repositories.CommandCodeRepository;
import com.paic.nbm.analyticsservice.Repositories.DiameterDictionaryRepository;
import com.paic.nbm.analyticsservice.Repositories.IpNamesDataRepository;
import com.paic.nbm.analyticsservice.Repositories.PeersDRARepository;
import com.paic.nbm.analyticsservice.Utils.UtilityFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._diameterCommandMap;
import static com.paic.nbm.analyticsservice.AnalyticsServiceApplication._diameterDraPeersMap;

@Service
@RequiredArgsConstructor
public class DiameterService {

    @Autowired
    EntityManager entityManager;

    private final PeersDRARepository peersDRARepository;
    private final CommandCodeRepository commandCodeRepository;
    private final DiameterDictionaryRepository diameterDictionaryRepository;
    private final IpNamesDataRepository ipNamesDataRepository;

    public List<DiameterDictionary> getAllDiameterDictionary() {
        return diameterDictionaryRepository.findAll();
    }

    public List<PeersDRA> getAllPeersDRA() {
        return peersDRARepository.findAll();
    }

    public List<IpNamesData> getAllIpNames() {
        return ipNamesDataRepository.findAll();
    }

    public List<CommandCodeCatalog> getAllCommandCode() {
        return commandCodeRepository.findAll();
    }

    public List<DiameterData> getGroupingCondition(String startDate, String endDate, String timezone, List<String> imsi, List<String> msisdn) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DiameterData> cq = cb.createQuery(DiameterData.class);
        Root<DiameterData> diameter = cq.from(DiameterData.class);
        List<Predicate> imsiMsisdnFilters = new ArrayList<>();
        Predicate dateFilter =  cb.between(diameter.get("time_epoch"), epochStartDate, epochEndDate);

        if (!imsi.isEmpty())
            imsiMsisdnFilters.add(diameter.get("imsi").in(imsi));

        if (!msisdn.isEmpty())
            imsiMsisdnFilters.add(diameter.get("msisdn").in(msisdn));

        Predicate finalPredicate = null;
        if (imsiMsisdnFilters.size() > 0)
            finalPredicate = cb.and(dateFilter, cb.or(imsiMsisdnFilters.toArray(Predicate[]::new)));
        else
            finalPredicate = dateFilter;

        cq.multiselect(diameter.get("msisdn"), diameter.get("imsi")).distinct(true);
        cq.where(finalPredicate);
        TypedQuery<DiameterData> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    public List<DiameterData> getSequenceDiagramQuery(HashMap<String, Object> filters_final, String timezone, int totalPerPage, int offSet) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DiameterData> query = cb.createQuery(DiameterData.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<DiameterData> diameter = query.from(DiameterData.class);
        List<Predicate> listFilter = new ArrayList<>();
        List<Predicate> listCustomFilterOr = new ArrayList<>();
        List<Predicate> listCustomFilterAnd = new ArrayList<>();
        Predicate dateFilter =  cb.between(diameter.get("time_epoch"), epochStartDate, epochEndDate);

        for (String key: filters_final.keySet()) {
            //The filter is a list or a custom filter
            if (key.split("-").length >= 2) {
                String[] data_key = key.split("-");
                switch (data_key[1]) {
                    case "list":
                        Expression<String> exp = diameter.get(data_key[0]);
                        if (data_key[0].equals("msisdn")) {
                            List<String> listMisdn = (List<String>) filters_final.get(key);
                            listFilter.add(exp.in(listMisdn));
                        } else if (data_key[0].equals("imsi")) {
                            List<String> listImsi = (List<String>) filters_final.get(key);
                            listFilter.add(exp.in(listImsi));
                        }
                        break;

                    case "custom":
                        HashMap<String,Object> customFilter = (HashMap<String, Object>) filters_final.get(key);
                        String filter = customFilter.get("filter").toString();
                        String value = customFilter.get("value").toString();
                        String type = customFilter.get("type").toString();
                        if (filter.equals("AND")) {
                            switch (type) {
                                case "BigInteger":
                                    BigInteger bigIntegerValue = BigInteger.valueOf(Long.parseLong(value));
                                    listCustomFilterAnd.add(cb.equal(diameter.get(data_key[0]), bigIntegerValue));
                                    break;

                                case "String":
                                    listCustomFilterAnd.add(cb.like(diameter.get(data_key[0]), value));
                                    break;
                            }

                        } else {
                            switch (type) {
                                case "BigInteger":
                                    BigInteger bigIntegerValue = BigInteger.valueOf(Long.parseLong(value));
                                    listCustomFilterOr.add(cb.equal(diameter.get(data_key[0]), bigIntegerValue));
                                    break;

                                case "String":
                                    listCustomFilterOr.add(cb.like(diameter.get(data_key[0]), value));
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
                    listFilter.add(diameter.get(key).in(Collections.singletonList(value)));
                else
                    listFilter.add(cb.like(diameter.get(key), value.toString()));
            }
        }

        Predicate filterPredicate = null;
        if (listFilter.size() > 0)
            filterPredicate = cb.and(dateFilter, cb.or(listFilter.toArray(Predicate[]::new)));
        else
            filterPredicate = dateFilter;

        Predicate filterOrPredicate = cb.or(listCustomFilterOr.toArray(Predicate[]::new));
        Predicate filterAndPredicate = cb.and(listCustomFilterAnd.toArray(Predicate[]::new));
        if (listCustomFilterOr.size() > 0) {
            query.where(filterPredicate, filterOrPredicate);
        } else if (listCustomFilterAnd.size() > 0) {
            query.where(filterPredicate, filterAndPredicate);
        } else {
            query.where(filterPredicate);
        }
        query.orderBy(
                cb.asc(diameter.get("time_epoch")),
                cb.asc(diameter.get("u_seconds_epoch")));
        List<DiameterData> result = entityManager.createQuery(query)
                .setMaxResults(totalPerPage)
                .setFirstResult(offSet)
                .getResultList();
        result.forEach(r -> {
            r.setOriginRealmValue(
                    _diameterDraPeersMap.containsKey(r.getSrc_ip()) ?
                            _diameterDraPeersMap.get((r.getSrc_ip())):
                            r.getSrc_ip());

            r.setDestinationRealmValue(
                    _diameterDraPeersMap.containsKey(r.getDst_ip()) ?
                            _diameterDraPeersMap.get((r.getDst_ip())):
                            r.getDst_ip());

            CommandCodeCatalog currentCommandCode = _diameterCommandMap.get(r.getCommand_code());
            if (r.getRequest()) {
                if (currentCommandCode.getCmd_request() == null) {
                    r.setCommand(r.getCommand_code() + " Request");
                } else {
                    r.setCommand(currentCommandCode.getCmd_request());
                }
            } else {
                if (currentCommandCode.getCmd_response() == null) {
                    r.setCommand(r.getCommand_code() + " Answer");
                } else {
                    r.setCommand(currentCommandCode.getCmd_response());
                }
            }
        });
        return result;
    }

    public List<DiameterData> getSequenceDiagram(List<DiameterData> diameterDataList) {
        diameterDataList.forEach(r -> {
            r.setOriginRealmValue(
                _diameterDraPeersMap.containsKey(r.getSrc_ip()) ?
                    _diameterDraPeersMap.get((r.getSrc_ip())):
                    r.getSrc_ip());

            r.setDestinationRealmValue(
                _diameterDraPeersMap.containsKey(r.getDst_ip()) ?
                    _diameterDraPeersMap.get((r.getDst_ip())):
                    r.getDst_ip());

            CommandCodeCatalog currentCommandCode = _diameterCommandMap.get(r.getCommand_code());
            if (r.getRequest()) {
                if (currentCommandCode == null || currentCommandCode.getCmd_request() == null) {
                    r.setCommand(r.getCommand_code() + " Request");
                } else {
                    r.setCommand(currentCommandCode.getCmd_request());
                }
            } else {
                if (currentCommandCode == null || currentCommandCode.getCmd_response() == null) {
                    r.setCommand(r.getCommand_code() + " Answer");
                } else {
                    r.setCommand(currentCommandCode.getCmd_response());
                }
            }
        });
        return diameterDataList;
    }

    public int getSequenceDiagramQueryCount(HashMap<String, Object> filters, String timezone) {
        HashMap<String, Object> filters_final = new HashMap<>(filters);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DiameterData> query = cb.createQuery(DiameterData.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<DiameterData> diameter = query.from(DiameterData.class);
        List<Predicate> listFilter = new ArrayList<>();
        List<Predicate> listCustomFilterOr = new ArrayList<>();
        List<Predicate> listCustomFilterAnd = new ArrayList<>();
        Predicate dateFilter =  cb.between(diameter.get("time_epoch"), epochStartDate, epochEndDate);

        for (String key: filters_final.keySet()) {
            //The filter is a list or a custom filter
            if (key.split("-").length >= 2) {
                String[] data_key = key.split("-");
                switch (data_key[1]){
                    case "list":
                        Expression<String> exp = diameter.get(data_key[0]);
                        if (data_key[0].equals("msisdn")) {
                            List<String> listMsisdn = (List<String>) filters_final.get(key);
                            listFilter.add(exp.in(listMsisdn));
                        } else if (data_key[0].equals("imsi")) {
                            List<String> listImsi = (List<String>) filters_final.get(key);
                            listFilter.add(exp.in(listImsi));
                        }
                        break;

                    case "custom":
                        HashMap<String,Object> customFilter = (HashMap<String, Object>) filters_final.get(key);
                        String filter = customFilter.get("filter").toString();
                        String value = customFilter.get("value").toString();
                        String type = customFilter.get("type").toString();
                        if (filter.equals("AND")) {
                            switch (type) {
                                case "BigInteger":
                                    BigInteger bigIntegerValue = BigInteger.valueOf(Long.parseLong(value));
                                    listCustomFilterAnd.add(cb.equal(diameter.get(data_key[0]), bigIntegerValue));
                                    break;

                                case "String":
                                    listCustomFilterAnd.add(cb.like(diameter.get(data_key[0]), value));
                                    break;
                            }

                        } else {
                            switch (type) {
                                case "BigInteger":
                                    BigInteger bigIntegerValue = BigInteger.valueOf(Long.parseLong(value));
                                    listCustomFilterOr.add(cb.equal(diameter.get(data_key[0]), bigIntegerValue));
                                    break;

                                case "String":
                                    listCustomFilterOr.add(cb.like(diameter.get(data_key[0]), value));
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
                    listFilter.add(diameter.get(key).in(Collections.singletonList(value)));
                else
                    listFilter.add(cb.like(diameter.get(key), value.toString()));
            }
        }

        Predicate filterPredicate = null;
        if (listFilter.size() > 0)
            filterPredicate = cb.and(dateFilter, cb.or(listFilter.toArray(Predicate[]::new)));
        else
            filterPredicate = dateFilter;

        Predicate filterOrPredicate = cb.or(listCustomFilterOr.toArray(Predicate[]::new));
        Predicate filterAndPredicate = cb.and(listCustomFilterAnd.toArray(Predicate[]::new));
        if (listCustomFilterOr.size() > 0) {
            query.where(cb.or(filterPredicate, filterOrPredicate));
        } else if (listCustomFilterAnd.size() > 0) {
            query.where(filterPredicate, filterAndPredicate);
        } else {
            query.where(filterPredicate);
        }
        return entityManager.createQuery(query).getResultList().size();
    }

    public List<DiameterData> getDiameterGroupingConditionForFrame(HashMap<String, Object> filters_final, String timezone) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DiameterData> query = cb.createQuery(DiameterData.class);
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("startDate").toString(), timezone);
        BigInteger epochEndDate = UtilityFunctions.parseStringDateToEpoch(filters_final.remove("endDate").toString(), timezone);
        Root<DiameterData> diameter = query.from(DiameterData.class);
        List<Predicate> listFilter = new ArrayList<>();
        List<Predicate> listCustomFilterOr = new ArrayList<>();
        List<Predicate> listCustomFilterAnd = new ArrayList<>();
        Predicate dateFilter =  cb.between(diameter.get("time_epoch"), epochStartDate, epochEndDate);

        for (String key: filters_final.keySet()) {
            //The filter is a list or a custom filter
            if (key.split("-").length >= 2) {
                String[] data_key = key.split("-");
                switch (data_key[1]){
                    case "list":
                        Expression<String> exp = diameter.get(data_key[0]);
                        if (data_key[0].equals("msisdn")) {
                            List<String> listMsisdn = (List<String>) filters_final.get(key);
                            listFilter.add(exp.in(listMsisdn));
                        } else {
                            if (data_key[0].equals("imsi")) {
                                List<String> listImsi = (List<String>) filters_final.get(key);
                                listFilter.add(exp.in(listImsi));
                            }
                        }
                        break;

                    case "custom":
                        HashMap<String,Object> customFilter = (HashMap<String, Object>) filters_final.get(key);
                        String type = customFilter.get("filter").toString();
                        String value = customFilter.get("value").toString();
                        if (type.equals("AND")) {
                            listCustomFilterAnd.add(cb.like(diameter.get(data_key[0]), value));
                        } else {
                            listCustomFilterOr.add(cb.like(diameter.get(data_key[0]), value));
                        }
                        break;
                }
            } else {
                String value = filters_final.get(key).toString();
                if (!value.isBlank() && !value.equals("NA"))
                    listFilter.add(cb.like(diameter.get(key), value));
            }
        }

        Predicate filterPredicate = null;
        if (listFilter.size() > 0)
            filterPredicate = cb.and(dateFilter, cb.or(listFilter.toArray(Predicate[]::new)));
        else
            filterPredicate = dateFilter;

        Predicate filterOrPredicate = cb.or(listCustomFilterOr.toArray(Predicate[]::new));
        Predicate filterAndPredicate = cb.and(listCustomFilterAnd.toArray(Predicate[]::new));
        if (listCustomFilterOr.size() > 0) {
            query.where(filterPredicate, filterOrPredicate);
        } if (listCustomFilterAnd.size() > 0) {
            query.where(filterPredicate, filterAndPredicate);
        } else {
            query.where(filterPredicate);
        }
        query.select(diameter.get("end_to_end_id")).distinct(true);

        return entityManager.createQuery(query).getResultList();
    }

    public List<DiameterWatchdogAlarm> getWatchdogAlarms(String startDate, String endDate, String timezone) {
        BigInteger epochStartDate = UtilityFunctions.parseStringDateToEpoch(startDate, timezone);
        BigInteger epochEndDate   = UtilityFunctions.parseStringDateToEpoch(endDate, timezone);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DiameterWatchdogAlarm> cq = cb.createQuery(DiameterWatchdogAlarm.class);
        Root<DiameterWatchdogAlarm> alarm = cq.from(DiameterWatchdogAlarm.class);

        cq.select(alarm).where(
                cb.between(alarm.get("time_epoch"), epochStartDate, epochEndDate)
        ).orderBy(cb.asc(alarm.get("time_epoch")));

        return entityManager.createQuery(cq).getResultList();
    }
}
