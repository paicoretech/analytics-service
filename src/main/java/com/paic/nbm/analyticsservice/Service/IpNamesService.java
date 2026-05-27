package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.IpNamesData;
import com.paic.nbm.analyticsservice.Repositories.IpNamesDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IpNamesService {

    private  final IpNamesDataRepository ipNamesDataRepository;
    public IpNamesData save (IpNamesData ipNamesData) {
        try {
            IpNamesData firstIPName = ipNamesDataRepository
                    .findAll(PageRequest.of(0,1, Sort.Direction.DESC,"id"))
                    .getContent().get(0);

            ipNamesData.setId(firstIPName.getId() + 1L);

            return ipNamesDataRepository.save(ipNamesData);

        } catch (Exception ex) {
            log.error("An error has occured while creating the new IpName : " + ex.getMessage());
            throw ex;
        }
    }


    public Boolean saveList (List<IpNamesData> ipNamesDataList) {
        try {
            ipNamesDataList.forEach(this::save);
            return true;
        } catch (Exception ex) {
            log.error("An error has occured while creating the IP name list : " + ex.getMessage());
            throw ex;
        }
    }
}
