package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/index")
public class IndexServlet extends HttpServlet {
    private SearchEngine searchEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        searchEngine = (SearchEngine) getServletContext().getAttribute("searchEngine");
        if (searchEngine == null) {
            searchEngine = new SearchEngine();
            getServletContext().setAttribute("searchEngine", searchEngine);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String folderPath = req.getParameter("folderPath");
        if (folderPath != null && !folderPath.isEmpty()) {
            try {
                searchEngine.indexFolder(folderPath);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"success\": true, \"message\": \"Folder indexed successfully\"}");
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"success\": false, \"message\": \"Error indexing folder: " + e.getMessage() + "\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\": false, \"message\": \"Folder path is required\"}");
        }
    }
}