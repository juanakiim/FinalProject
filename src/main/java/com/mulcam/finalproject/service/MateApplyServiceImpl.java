package com.mulcam.finalproject.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mulcam.finalproject.dao.MateApplyDAO;
import com.mulcam.finalproject.dto.MateApplyDTO;
import com.mulcam.finalproject.entity.Mate;
import com.mulcam.finalproject.entity.MateApply;
import com.mulcam.finalproject.entity.User;

@Service
public class MateApplyServiceImpl implements MateApplyService {

	@Autowired
	MateApplyDAO mateApplyDAO;

	@Autowired
	MateService mateService;

	@Override
	public Long save(MateApply mateApply) {
		mateApplyDAO.save(mateApply);
		return mateApplyDAO.findSaveId();
	}
	
	@Override
	public void delete(Long aid) {
		mateApplyDAO.delete(aid);
	}

	@Override
	public List<MateApplyDTO> findByUid(User user) {
		List<MateApplyDTO> dtoList = new ArrayList<>();
		List<MateApply> list = mateApplyDAO.findByUid(user.getIdAuto());

		// mapper
		ModelMapper modelMapper = new ModelMapper();
		list.forEach(entity -> {
			MateApplyDTO applyDTO = modelMapper.map(entity, MateApplyDTO.class);
			Long mid = entity.getMid();
			applyDTO.setUser(user);
			applyDTO.setMate(mateService.findById(mid).get());
			dtoList.add(applyDTO);
		});

		return dtoList;
	}

	@Override
	public List<MateApplyDTO> findByMid(Mate mate) {
		// TODO Auto-generated method stub
		return null;
	}



}
