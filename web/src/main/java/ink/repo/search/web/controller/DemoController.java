package ink.repo.search.web.controller;

import ink.repo.search.common.dto.QueryServerResponse;
import ink.repo.search.web.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DemoController {
    @Autowired
    SearchService searchService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @RequestMapping("/search")
    public String hello(Model model, @RequestParam(value="query", required=true, defaultValue="World") String query,
                        @RequestParam(value="page", required=false, defaultValue="1") String page) {
        // TODO: Reject empty query
        int pageNum;
        try {
            pageNum = Integer.parseInt(page);
        } catch(NumberFormatException e) {
            // TODO: Return error page informing bad request
            return "demo";
        }
        QueryServerResponse queryServerResponse = searchService.search(query, pageNum);
        System.out.println(queryServerResponse);
        model.addAttribute("queryResult", queryServerResponse);

        return "demo";
    }
}

