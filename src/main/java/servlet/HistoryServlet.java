package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@WebServlet("/history")
public class HistoryServlet extends HttpServlet {
    private List<SearchEntry> searchHistory = new ArrayList<>();

    private static class SearchEntry {
        String term;
        Instant time;

        SearchEntry(String term, Instant time) {
            this.term = term;
            this.time = time;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sortBy = req.getParameter("sortBy");
        sortHistory(sortBy);

        ObjectMapper mapper = new ObjectMapper();
        List<ObjectNode> jsonHistory = new ArrayList<>();
        for (SearchEntry entry : searchHistory) {
            ObjectNode node = mapper.createObjectNode();
            node.put("term", entry.term);
            node.put("time", entry.time.toString());
            jsonHistory.add(node);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), jsonHistory);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String term = mapper.readTree(req.getReader()).get("term").asText();
        searchHistory.add(new SearchEntry(term, Instant.now()));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void sortHistory(String sortBy) {
        if ("time".equals(sortBy)) {
            bubbleSortByTime(searchHistory);
        } else {
            bubbleSortByTerm(searchHistory);
        }
    }

    private void bubbleSortByTerm(List<SearchEntry> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (compareEntries(list.get(j), list.get(j + 1)) > 0) {

                    SearchEntry temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    private void bubbleSortByTime(List<SearchEntry> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).time.compareTo(list.get(j + 1).time) > 0) {

                    SearchEntry temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    private int compareEntries(SearchEntry e1, SearchEntry e2) {
        int nameCompare = e1.term.compareTo(e2.term);
        if (nameCompare != 0) {
            return nameCompare;
        }
        return e1.time.compareTo(e2.time);
    }
}