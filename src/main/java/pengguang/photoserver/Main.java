package pengguang.photoserver;

import java.io.*;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.text.Collator;

/**
 * Servlet implementation class LoginController
 */
public class Main extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		out.println("<HTML>");
		out.println("<HEAD><meta http-equiv=Content-Type content='text/html;charset=utf-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><TITLE>Album</TITLE>");
		out.println("<link rel='stylesheet' type='text/css' href='css/style.css'>");
		out.println("<script src='js/misc.js'></script>");
		out.println("</HEAD>");
		out.println("<BODY>");
		out.println("<table width='100%'><tr><td><form action='index.html' style='display: inline;'> <input type='submit' value='刷新' /></form></td><td align='right'><form action='index.html' style='display: inline;' method='post' id='form_add_album'><nobr><button type='button' class='button' onclick='addAlbum()'>添加相册</button><input type='hidden' id='album' name='album' value='none'/></form></td></tr></table>");
		out.println("<br/>");

		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {
				if (file.isHidden())
					return false;
				if (file.isDirectory())
					return true;
				return false;
			}
		};
		File path = new File(Util.ROOT_PATH);
		File[] files = path.listFiles(filter);
		if (files != null) {
			Arrays.sort(files, new Comparator<File>() {
			    public int compare(File f1, File f2) {
			        Collator cmp = Collator.getInstance();
			        return -1*cmp.compare(f1.getName(), f2.getName());
                }
            });
		}
		for (File file: files) {
			String album = file.getName();

			File[] filesAlbum = new File(Util.ROOT_PATH+"/"+album).listFiles(new FileFilter() {
				public boolean accept(File file) {
					if (file.isHidden())
					return false;
					if (file.isDirectory())
					return false;

					String name = file.getName();
					if (name.substring(name.lastIndexOf('.')+1).toLowerCase().matches("jpeg|jpg|mp4")) return true;
					return false;
				}
			});
			int total = 0;
			if (filesAlbum != null) total = filesAlbum.length;
			out.println(String.format("<div class='gallery'><a href='album.html?album=%s'><img src='img/blank.png' width='400' height='400'></a><div class='desc'>%s(%d)</div></div>", URLEncoder.encode(album, "UTF-8"), album, total));
		}

		out.println("</table>");
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		String album = request.getParameter("album");
		File f = new File(Util.ROOT_PATH+"/"+album);
		if (f.mkdir()) {
			response.sendRedirect("album.html?album="+URLEncoder.encode(album, "UTF-8"));
		} else {
			out.println(Util.getErrorPage("返回", "main.html", "添加失败"));
		}
	}
}

