package com.mulcam.finalproject.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.mulcam.finalproject.entity.MateApply;

@Mapper
public interface MateApplyDAO {

	@Insert("INSERT INTO mate_apply VALUES(DEFAULT, #{content}, DEFAULT, DEFAULT, DEFAULT, #{mid}, #{uid}, #{applyTradelType}, DEFAULT)")
	public void save(MateApply mateApply);

	@Select("SELECT LAST_INSERT_ID();")
	public Long findSaveId();

	@Update("UPDATE mate_apply"
			+ "	SET isDel = 1"
			+ "	WHERE aid = #{aid};")
	public void delete(Long aid);

	@Update("UPDATE mate_apply"
			+ "	SET isApply = #{isApply},"
			+ "	modDate = DEFAULT"
			+ "	WHERE aid = #{aid};")
	public void editIsApply(Long aid, int isApply);

	@Select("SELECT modDate FROM mate_apply"
			+ "	WHERE aid = #{aid};")
	public LocalDateTime findEditTime(Long aid);

	@Select("SELECT * FROM mate_apply WHERE isDel = 0 ORDER BY modDate DESC, regdate DESC;")
	public List<MateApply> findAll();

	/** SEND APPLY : 내가 신청한 리스트 조회 */
	@Select("SELECT * FROM mate_apply WHERE uid = #{uid} AND isDel = 0 ORDER BY modDate DESC, regDate DESC;")
	public List<MateApply> findBySendUid(Long uid);

	/** SEND APPLY New Notify : 오늘 상태 변경된 리스트 카운트 */
	@Select("SELECT COUNT(*) FROM mate_apply"
			+ "	WHERE CAST(modDate AS DATE) = CURDATE()"
			+ "	AND isDel = 0"
			+ "	AND uid = #{uid};")
	public int findNewBySendUid(Long uid);

	/** GET APPLY : 내가 작성한 글의 신청 리스트 조회 */
	@Select("SELECT a.* FROM mate_apply AS a"
			+ " JOIN mate AS m"
			+ " ON a.`mid` = m.`mid`"
			+ " WHERE isDel = 0 AND m.uid = #{uid}"
			+ " ORDER BY modDate DESC, regDate DESC;")
	public List<MateApply> findByGetUid(Long uid);

	/** GET APPLY New Notify : 오늘 상태 변경된 리스트 카운트 */
	@Select("SELECT COUNT(*) FROM mate_apply AS a"
			+ " JOIN mate AS m"
			+ " ON a.`mid` = m.`mid`"
			+ " WHERE CAST(modDate AS DATE) = CURDATE()"
			+ "	AND a.isDel = 0"
			+ " AND m.uid = #{uid};")
	public int findNewByGetUid(Long uid);


	@Select("SELECT * FROM mate_apply WHERE mid = #{mid} AND isDel = 0 ORDER BY modDate DESC, regDate DESC;")
	public List<MateApply> findByMid(Long mid);



}
