package ink.repo.search.web.controller;

import ink.repo.search.common.dto.EvaluateResponse;
import ink.repo.search.common.dto.SearchResponse;
import ink.repo.search.web.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {
    @Autowired
    SearchService searchService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @RequestMapping("/search")
    public String searchResults(Model model, @RequestParam(value="query", required=true) String query,
                        @RequestParam(value="page", required=false, defaultValue="1") String page) {
        int pageNum;
        try {
            pageNum = Integer.parseInt(page);
        } catch(NumberFormatException e) {
            // TODO: Return error page informing bad request
            return "400";
        }
        if (query.length() == 0) {
            return "400";
        }
        SearchResponse searchResponse = searchService.search(query, pageNum);
        model.addAttribute("queryResult", searchResponse);

        return "searchResults";
    }

    @RequestMapping("/evaluate")
    public String evaluate(Model model, @RequestParam(value="query", required=true) String query,
                        @RequestParam(value="page", required=false, defaultValue="1") String page) {
        // TODO: Reject empty query
        int pageNum;
        try {
            pageNum = Integer.parseInt(page);
        } catch(NumberFormatException e) {
            // TODO: Return error page informing bad request
            return "400";
        }
        EvaluateResponse evaluateResponse = searchService.evaluate(query);
        model.addAttribute("queryResult", evaluateResponse);

        return "evaluate";
    }
}

