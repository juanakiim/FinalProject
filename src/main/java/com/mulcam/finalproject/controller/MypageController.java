package com.mulcam.finalproject.controller;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mulcam.finalproject.dto.CalendarDTO;
import com.mulcam.finalproject.dto.ChartDTO;
import com.mulcam.finalproject.dto.MateApplyDTO;
import com.mulcam.finalproject.dto.MypageSumDTO;
import com.mulcam.finalproject.dto.UserDTO;
import com.mulcam.finalproject.entity.Cash;
import com.mulcam.finalproject.service.CSuccessService;
import com.mulcam.finalproject.service.CashListService;
import com.mulcam.finalproject.service.MateApplyService;
import com.mulcam.finalproject.service.MypageService;

@Controller
@RequestMapping("/mypage")
public class MypageController {

	@Autowired
	private CSuccessService css;

	@Autowired
	private MateApplyService applyService;

	@Autowired
	private MypageService mypageService;

	@Autowired
	private CashListService cashListService;

	/** MyPage */
	@GetMapping("/main")
	public String mypageGet(@ModelAttribute("calendar") CalendarDTO calendarDTO, HttpSession session) {
		UserDTO user = (UserDTO) session.getAttribute("user");
		calendarDTO.setUid(user.getId());
		calendarDTO = mypageService.getCalendar(calendarDTO);
		return "mypage/mypage";
	}

	@PostMapping("/main")
	@ResponseBody
	public MypageSumDTO mypagePost(HttpSession session) {
		UserDTO user = (UserDTO) session.getAttribute("user");
		MypageSumDTO mypageSumDTO = css.getSum(user.getId());
		return mypageSumDTO;
	}
	
	/* 수입지출 등록 리스트 */
	@GetMapping(value = {"cash/list", "cash/list/{arrow}"})
	public String getCashList(HttpServletRequest req, Model model, @PathVariable(required = false) String arrow) {

		HttpSession session = req.getSession();
		UserDTO user = (UserDTO) session.getAttribute("user");
		int month = LocalDate.now().getMonthValue();
		LocalDate today = LocalDate.now();
		int year = 2000;
		
		String sessionMonthYear = (String) session.getAttribute("monthYear"); 
		if(sessionMonthYear == null) {
			year = today.getYear();
			month = today.getMonthValue();
		} else {
			year = Integer.parseInt(sessionMonthYear.substring(0,4));
			month = Integer.parseInt(sessionMonthYear.substring(5));
		}
		
		String startDate = null;
		String endDate = null;
		LocalDate startDay;
		
		if(arrow !=null) {
			switch (arrow) {
			case "left":
				month = month -1;
				if(month == 0 ) {
					month = 12;
					year = year -1 ;
				} 
				startDate = String.format("%d-%02d-01", year, month); // 시작날짜가 해당 월의 1일 
				startDay = LocalDate.parse(String.format("%d-%02d-01", year, month));
				endDate = startDay.withDayOfMonth(startDay.lengthOfMonth()).toString(); 
				break;

			case "right" :
				month = month + 1;
				if(month == 13) {
					month = 1;
					year = year + 1;
				}
				startDate = String.format("%d-%02d-01", year, month); // 시작날짜가 해당 월의 1일 
				startDay = LocalDate.parse(String.format("%d-%02d-01", year, month));
				endDate = startDay.withDayOfMonth(startDay.lengthOfMonth()).toString(); 
				break;
			}
		} else { 			
			/* 사용자 지정 기간별 수입 지출 합계구하기 */
			startDate = req.getParameter("startDate");
			if(startDate==null || startDate.equals(""))
				startDate = today.with(TemporalAdjusters.firstDayOfMonth()).toString();
			endDate = req.getParameter("endDate");
			if(endDate==null || endDate.equals(""))
				endDate = today.toString();

		}
		
		sessionMonthYear = String.format("%d.%02d", year, month);
		session.setAttribute("monthYear", sessionMonthYear);
		
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("year", year);
		model.addAttribute("month", month);
		
		List<Cash> list = cashListService.getList(user.getId(), startDate, endDate);
		int incomeSum = 0, expenseSum=0;
		for(Cash cash : list) {
			if(cash.getCategory()==0)
				expenseSum += cash.getAmount();
			else
				incomeSum += cash.getAmount();
		}
		model.addAttribute("incomeSum", incomeSum); // 달의 첫째날 ~ 오늘날짜까지의 수입합 
		model.addAttribute("expenseSum", expenseSum); // 달의 첫째날 ~ 오늘날짜까지의 지출합 
		
		
		/* 오늘의 지출수입 합계 구하기 */
		int expenseTodaySum = cashListService.sumNowExpense(user.getId());
		int incomeTodaySum=cashListService.sumNowIncome(user.getId());
		model.addAttribute("expenseTodaySum", expenseTodaySum); // 오늘 지출합
		model.addAttribute("incomeTodaySum", incomeTodaySum);   // 오늘 수입합
		
		/* 한달치 리스트 출력 */  
		Map<String, List<Cash>> map = cashListService.getCashListByPeriod(user.getId(),startDate, endDate);
		model.addAttribute("map",map);
		return "cash/list";
	}

	@GetMapping("/main/cash/{date}")
	@ResponseBody
	public List<Cash> mypageCashListGet(@PathVariable String date, HttpSession session) {
		UserDTO user = (UserDTO) session.getAttribute("user");
		List<Cash> list = cashListService.getList(user.getId(), date, date);
		return list;
	}

	@GetMapping("/mate/apply/all")
	public String applyGet(Model model, HttpSession session) {
		UserDTO user = (UserDTO) session.getAttribute("user");
		Long uid = user.getUid();

		List<MateApplyDTO> sendApply = applyService.findBySendUid(uid);
		model.addAttribute("sendApply", sendApply);
		model.addAttribute("sendNew", applyService.findNewBySendUid(uid)); // New Notify

		List<MateApplyDTO> getApply = applyService.findByGetUid(uid);
		model.addAttribute("getApply", getApply);
		model.addAttribute("getNew", applyService.findNewByGetUid(uid)); // New Notify

		return "mypage/apply_list_all";
	}
	
	@GetMapping("/chart")
	public String ChartGet() {
		return "redirect:/mypage/chart/cash";
	}
	
	@GetMapping("/chart/cash")
	public String ChartCashGet() {
		return "mypage/chart_cash";
	}
	
	@PostMapping("/chart/cash")
	@ResponseBody
	public List<ChartDTO> ChartCashPost(HttpSession session) {
		UserDTO user = (UserDTO) session.getAttribute("user");
		return mypageService.getCashChart(user);
	}
	

}
