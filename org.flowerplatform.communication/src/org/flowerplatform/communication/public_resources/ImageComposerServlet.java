/* license-start
 * 
 * Copyright (C) 2008 - 2013 Crispico, <http://www.crispico.com/>.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details, at <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *   Crispico - Initial API and implementation
 *
 * license-end
 */
package org.flowerplatform.communication.public_resources;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.flowerplatform.common.plugin.AbstractFlowerJavaPlugin;

/**
 * @author Mariana
 */
public class ImageComposerServlet extends PublicResourcesServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String PATH_PREFIX = "/image-composer";

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String requestedFile = request.getPathInfo();
		if (requestedFile.startsWith(PATH_PREFIX)) {
			requestedFile = requestedFile.substring(PATH_PREFIX.length());
		}
		
		int indexOfSecondSlash = requestedFile.indexOf('/', 1); // 1, i.e. skip the first index
		if (indexOfSecondSlash < 0) {
			send404(request, response);
			return;
		}
		
		// both variables are prefixed with /
		String plugin = requestedFile.substring(0, indexOfSecondSlash);
		String[] paths = requestedFile.substring(indexOfSecondSlash).split("\\+");
		
		BufferedImage result = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = result.createGraphics();
		
		for (String path : paths) {
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			requestedFile = "platform:/plugin" + plugin + "/" + AbstractFlowerJavaPlugin.PUBLIC_RESOURCES_DIR + path;
			try {
				URL url = new URL(requestedFile);
				BufferedImage image = ImageIO.read(url);
				graphics.drawImage(image, null, 0, 0);
			} catch (Exception e) {
				// one of the images was not found; skip it
			}
		}
		
		graphics.dispose();
		
		OutputStream output = null;
		output = response.getOutputStream();
		ImageIO.write(result, "png", output);
	}

}