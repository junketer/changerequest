package com.oag.maven.plugin.changerequest;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which raises a CR request.
 * 
 */
@Mojo(name = "raise", defaultPhase = LifecyclePhase.PACKAGE)
public class Raise extends AbstractMojo {
	/**
	 * Location of the file.
	 * 
	 * @required
	 */
	@Parameter(required = true, property = "raise.outputDirectory", defaultValue = "${project.build.directory}")
	private File outputDirectory;

	/**
	 * Location of the changeRequestDir
	 */
	@Parameter(required = true, property = "raise.changeRequestDirectory", defaultValue = "/home/dan/changeRequestDir")
	private File changeRequestDirectory;

	/**
	 * Change request post URL
	 */
	@Parameter(required = true, property = "raise.crUrl", defaultValue = "mailto:dtillin@oag.com")
	private URL crUrl;

	/**
	 * The author of the CR. Defaults to the current user name
	 */
	@Parameter(required = true, property = "raise.author", defaultValue = "${user.name}")
	private String author;

	/**
	 * The tester of the CR. Defaults to the current user name
	 */
	@Parameter(required = true, property = "raise.tester", defaultValue = "${user.name}")
	private String tester;

	/**
	 * The title for the CR. Defaults to the build final name
	 */
	@Parameter(required = true, property = "raise.title", defaultValue = "${project.build.finalName}")
	private String title;

	@Parameter(required = true, property = "raise.crType", defaultValue = "Windows")
	private String crType;

	@Parameter(required = true, property = "raise.buildFile", defaultValue = "${project.build.finalName}")
	private String buildFile;
	
	@Parameter(required = true, property = "raise.buildFileExt", defaultValue = "${project.packaging}")
	private String buildFileExt;
	

	public void execute() throws MojoExecutionException, MojoFailureException {
		File f = outputDirectory;

		if (!f.exists()) {
			f.mkdirs();
		}

		try {
			String crTargetDirPath =changeRequestDirectory+"/"+title; 
			
			if (changeRequestDirectory != null
					&& changeRequestDirectory.exists()) {
				// Use the title as the name of the sub-dir to create
				if (title!=null && title.length()>0) {
					File crTargetDir = new File(crTargetDirPath);
					if (!crTargetDir.exists()) {
						// create the directory
						if(!crTargetDir.mkdir()) {
							throw new MojoFailureException("Could not create CR directory :" + crTargetDir);						
						}
					};
				} else {
					// title is used as the sub dir name, so if null or 0-len string, raise an error
					throw new MojoFailureException("Could not create CR directory :" + crTargetDirPath);
				}
				File destFile = new File(crTargetDirPath
						+ "/" + buildFile+"."+buildFileExt);
				// copy the built output file to the CR directory
				FileUtils.copyFile(new File(outputDirectory.getAbsolutePath()
						+ "/" + buildFile+"."+buildFileExt), destFile);
				
				// raise CR via web page defined by crURL
				PostMethod post = new PostMethod(crUrl.toExternalForm());
				post.addParameter("author", author);
				post.addParameter("tester", tester);
				post.addParameter("title", title);
				HttpClient client = new HttpClient();
				client.executeMethod(post);
			} else {
				getLog().error(
						"CR directory not specified or doesn't exist: "
								+ changeRequestDirectory);
			}

			getLog().warn("author: " + author);
			getLog().warn("tester: " + tester);
			getLog().warn("title: " + title);
			getLog().warn("crUrl: " + crUrl);
			getLog().warn("crType: " + crType);

		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(),e);
		} 
	}
}
