/*
 * Copyright 2018 University of Padua, Italy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ap.mnemosyne.resources;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represents a generic resource.
 * 
 * @author Nicola Ferro (ferro@dei.unipd.it)
 * @version 1.00
 * @since 1.00
 */

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
		@JsonSubTypes.Type(value = Task.class),
		@JsonSubTypes.Type(value = User.class),
		@JsonSubTypes.Type(value = Message.class),
		@JsonSubTypes.Type(value = TaskConstraint.class),
		@JsonSubTypes.Type(value = Point.class),
		@JsonSubTypes.Type(value = Place.class),
		@JsonSubTypes.Type(value = Hint.class),
		@JsonSubTypes.Type(value = Parameter.class)
})
public abstract class Resource {

	/**
	 * The JSON factory to be used for creating JSON parsers and generators.
	 */
	protected static final JsonFactory JSON_FACTORY;

	static {
		// setup the JSON factory
		JSON_FACTORY = new JsonFactory();
		JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
		JSON_FACTORY.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
	}

	/**
	/**
	 * Returns a JSON representation of the {@code Resource} into the given {@code OutputStream}.
	 *
	 * @param out  the stream to which the JSON representation of the {@code Resource} has to be written.
	 *
	 * @throws IOException if something goes wrong during the parsing.
	 */
	public abstract void toJSON(final OutputStream out) throws IOException;

	public abstract String toJSON() throws IOException;

	public abstract void toJSON(final PrintWriter pw) throws IOException;

	public static Resource fromJSON(InputStream in) throws IOException
	{
		StringBuilder textBuilder = new StringBuilder();
		try (Reader reader = new BufferedReader(new InputStreamReader
				(in, Charset.forName(StandardCharsets.UTF_8.name())))) {
			int c = 0;
			while ((c = reader.read()) != -1) {
				textBuilder.append((char) c);
			}
		}
		ObjectMapper om = new ObjectMapper();
		om.findAndRegisterModules();
		Parameter p = om.readValue(textBuilder.toString(), Parameter.class);
		return p;
	}
}
