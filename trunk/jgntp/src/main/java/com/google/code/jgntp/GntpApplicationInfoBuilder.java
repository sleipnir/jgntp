/*
 * Copyright (C) 2010 Leandro de Oliveira Aparecido <lehphyro@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.jgntp;

import java.awt.image.*;
import java.net.*;

import com.google.code.jgntp.internal.*;
import com.google.common.base.*;

public class GntpApplicationInfoBuilder {

	private String name;
	private RenderedImage iconImage;
	private URI iconUri;

	public GntpApplicationInfoBuilder(String name) {
		Preconditions.checkNotNull(name, "Application name must not be null");
		this.name = name;
	}

	public GntpApplicationInfoBuilder name(String name) {
		Preconditions.checkNotNull(name, "Application name must not be null");
		this.name = name;
		return this;
	}

	public GntpApplicationInfoBuilder icon(RenderedImage image) {
		this.iconImage = image;
		this.iconUri = null;
		return this;
	}

	public GntpApplicationInfoBuilder icon(URI uri) {
		this.iconUri = uri;
		this.iconImage = null;
		return this;
	}
	
	public GntpApplicationInfo build() {
		return new GntpApplicationInfoDefaultImpl(name, iconImage, iconUri);
	}

}
