package pengguang.photoserver;

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Servlet implementation class LoginController
 */
public class Album extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		String album = request.getParameter("album");
		if (album == null) {
			album = "";
		}

		out.println("<HTML>");
		out.println("<HEAD><meta http-equiv=Content-Type content='text/html;charset=utf-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><TITLE>Album</TITLE>");
		out.println("<link rel='stylesheet' type='text/css' href='css/style.css'>");
		out.println("<script src='js/misc.js'></script>");
		out.println("</HEAD>");
		out.println("<BODY>");

		File path = new File(Util.ROOT_PATH+"/"+album);
		File[] files = path.listFiles(new FileFilter() {
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
		if (files != null) {
			Arrays.sort(files);
		}

		out.println("<table width='100%'><tr><td valign='top'><form action='index.html' style='display: inline;'> <input type='hidden' name='album' value='"+album+"'/><input type='submit' value='返回' /></form></td><td align='right' valign='top'><button class='button' onclick='toggleUploadDiv()'>添加</button></td></tr></table>");
		out.println("<div id='uploadDiv' style='display: none'><form action='album.html' style='display: inline;' method='post' enctype='multipart/form-data'><table width='100%'><tr><td></td><td align='right'><input type='file' name='file' accept='image/*,video/*' multiple='true'/><input type='hidden' name='album' value='"+album+"'/><input type='submit' value='上传'/></td></tr></table></form></div>");

		for (File file: files) {
			String name = file.getName();
			if (name.substring(name.lastIndexOf('.')+1).toLowerCase().matches("jpeg|jpg")) {
				String fullPath = String.format("%s/%s/%s/%s",  Util.ROOT_PATH, album, ".thumbnail", name);
				if (!new File(fullPath).exists()) {
					fullPath = String.format("%s/%s/%s",  Util.ROOT_PATH, album, name);
				}
				out.println(String.format("<div class='gallery'><a href='photo.html?album=%s&photo=%s'><img src='%s' width='640'></a><div class='desc'>%s</div></div>", URLEncoder.encode(album, "UTF-8"), URLEncoder.encode(name, "UTF-8"), fullPath, name));
			} else {
				out.println(String.format("<div class='gallery'><a href='photo.html?album=%s&photo=%s'><video width='640' controls><source src='%s/%s/%s' type='video/mp4'></video></a><div class='desc'>%s</div></div>", URLEncoder.encode(album, "UTF-8"), URLEncoder.encode(name, "UTF-8"), Util.ROOT_PATH, album, name, name));
			}
		}

		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");

		String album = request.getParameter("album");

		List<Part> fileParts = request.getParts().stream().filter(part -> "file".equals(part.getName())).collect(Collectors.toList()); // Retrieves <input type="file" name="file" multiple="true">
		for (Part filePart : fileParts) {
			try {
				String filename = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
				InputStream fileContent = filePart.getInputStream();
				String path = String.format("%s/%s/%s", Util.ROOT_PATH, album, filename);
				Log.d("upload "+path);
				File file = new File(path);
				if (file.exists()) {
					continue;
				}

				byte[] buf = new byte[512];
				int len = 0;
				FileOutputStream os = new FileOutputStream(file);
				while ((len = fileContent.read(buf)) > 0) {
					os.write(buf, 0, len);
				}
				os.close();

				/* generate thumbnail */
				try {
					String thumbDir = String.format("%s/%s/.thumbnail", Util.ROOT_PATH, album);
					File fileDir = new File(thumbDir);
					if (!fileDir.exists()) fileDir.mkdir();

					String thumb = String.format("%s/%s/.thumbnail/%s", Util.ROOT_PATH, album, filename);
					new Thumbnail().generate(path, thumb, 320, 320);
				} catch (Exception e) {
					Log.d(""+e);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		response.sendRedirect("album.html?album="+URLEncoder.encode(album, "UTF-8"));
	}
}

