/*
 * Copyright (c) 2010-2018 Evolveum
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

package com.evolveum.midpoint.gui.impl.component.icon;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author skublik
 */
public class CompositedIcon implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String basicIcon;
	private List<String> layerIcons;
	private String basicIconHtmlColor = "";
	
	
	public CompositedIcon(String basicIcon, List<String> layerIcons, String basicIconHtmlColor){
		this.basicIcon = basicIcon;
		this.layerIcons = layerIcons;
		this.basicIconHtmlColor = basicIconHtmlColor;
	}
	
	public String getBasicIcon() {
		return basicIcon;
	}
	
	public List<String> getLayerIcons() {
		return layerIcons;
	}

	public String getBasicIconHtmlColor() {
		return basicIconHtmlColor;
	}

	public boolean hasLayerIcons(){
		return getLayerIcons() != null && !getLayerIcons().isEmpty();
	}

	public boolean hasBasicIcon() {
		return StringUtils.isNotEmpty(getBasicIcon());
	}

	public boolean hasBasicIconHtmlColor() {
		return StringUtils.isNotEmpty(getBasicIconHtmlColor());
	}
}
