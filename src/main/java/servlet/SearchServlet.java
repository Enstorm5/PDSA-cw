package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@WebServlet({"/search", "/open", "/view", "/preview"})
public class SearchServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/search".equals(path)) {
            handleSearch(req, resp);
        } else if ("/open".equals(path)) {
            handleOpen(req, resp);
        } else if ("/view".equals(path)) {
            handleView(req, resp);
        } else if ("/preview".equals(path)) {
            handlePreview(req, resp);
        }
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = req.getParameter("q");
        List<SearchEngine.SearchResult> results = searchEngine.search(query);

        ObjectMapper mapper = new ObjectMapper();
        String jsonResults = mapper.writeValueAsString(results);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResults);
    }

    private void handleOpen(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filePath = req.getParameter("path");
        String searchWord = req.getParameter("word");

        if (filePath != null && searchWord != null) {
            File file = new File(filePath);
            if (file.exists()) {
                resp.setContentType("application/octet-stream");
                resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                Files.copy(file.toPath(), resp.getOutputStream());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("File not found");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing file path or search word");
        }
    }

    private void handleView(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filePath = req.getParameter("path");
        String searchWord = req.getParameter("word");

        if (filePath != null && searchWord != null) {
            File file = new File(filePath);
            if (file.exists()) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".pdf")) {
                    resp.setContentType("application/pdf");
                } else if (fileName.endsWith(".txt")) {
                    resp.setContentType("text/plain");
                } else {

                    resp.setContentType("application/octet-stream");
                }
                resp.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
                Files.copy(file.toPath(), resp.getOutputStream());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("File not found");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing file path or search word");
        }
    }

    private void handlePreview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filePath = req.getParameter("path");
        String searchWord = req.getParameter("word");

        if (filePath != null && searchWord != null) {
            File file = new File(filePath);
            if (file.exists()) {
                String content = getFileContent(file);
                String previewContent = getPreviewContent(content, searchWord);
                resp.setContentType("text/plain");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(previewContent);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("File not found");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing file path or search word");
        }
    }

    private String getFileContent(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else {
            return new String(Files.readAllBytes(file.toPath()));
        }
    }

    private String getPreviewContent(String content, String searchWord) {
        int index = content.toLowerCase().indexOf(searchWord.toLowerCase());
        if (index != -1) {
            int start = Math.max(0, index - 200);
            int end = Math.min(content.length(), index + searchWord.length() + 200);
            return content.substring(start, end);
        }
        return "Preview not available";
    }
}