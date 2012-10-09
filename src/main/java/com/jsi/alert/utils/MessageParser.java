package com.jsi.alert.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.activemq.util.ByteArrayInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jsi.alert.model.notification.EventNotification;
import com.jsi.alert.model.notification.IdentityNotification;
import com.jsi.alert.model.notification.IssueNotification;
import com.jsi.alert.model.notification.ItemNotification;
import com.jsi.alert.model.notification.Notification;

public class MessageParser {
	
	private static final Logger log = LoggerFactory.getLogger(MessageParser.class);
	
	private enum ItemType {
		EMAIL(10),
		POST(11),
		BUG_DESCRIPTION(12),
		BUG_COMMENT(13),
		COMMIT(14),
		WIKI_POST(15);
		
		public final int value;
		
		ItemType(int value) {
			this.value = value;
		}
	}
	
	private static final Comparator<String> MONTH_LABEL_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String m1, String m2) {
			String[] m1Pr = m1.split("-");
			String[] m2Pr = m2.split("-");
			
			int val1 = Integer.parseInt(m1Pr[0])*100 + Integer.parseInt(m1Pr[1]);
			int val2 = Integer.parseInt(m2Pr[0])*100 + Integer.parseInt(m2Pr[1]);
			
			if (val1 > val2)
				return 1;
			else if (val1 < val2)
				return -1;
			else return 0;
		}
	};
	
	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	
	private static final int[] MONTH_LENGTHS = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

	@SuppressWarnings("unchecked")
	public static String parseAPICommitDetailsMessage(String responseMsg) {
		try {
			JSONObject result = new JSONObject();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes("UTF-8")));
			
			JSONArray filesArr = new JSONArray();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
			
			Node respData = doc.getElementsByTagName("s3:responseData").item(0);
			NodeList propNodes = respData.getChildNodes();
			for (int nodeIdx = 0; nodeIdx < propNodes.getLength(); nodeIdx++) {
				Node node = propNodes.item(nodeIdx);
				String nodeName = node.getNodeName();
				
				if ("s3:commitRepositoryUri".equals(nodeName))
					result.put("repositoryUri", node.getTextContent());
				else if ("s3:commitRevisionTag".equals(nodeName)) 
					result.put("revisionTag", node.getTextContent());
				else if ("s3:commitDate".equals(nodeName))
					result.put("commitDate", dateFormat.parse(node.getTextContent()).getTime());
				else if ("s3:commitMessageLog".equals(nodeName))
					result.put("message", Utils.escapeHtml(node.getTextContent()));
				else if ("s3:commitAuthor".equals(nodeName)) {
					JSONObject authorJSon = new JSONObject();
					
					NodeList authorProps = node.getChildNodes();
					for (int i = 0; i < authorProps.getLength(); i++) {
						Node prop = authorProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						authorJSon.put(label, value);
					}
					result.put("author", authorJSon);
				}
				else if ("s3:commitCommitter".equals(nodeName)) {
					JSONObject authorJSon = new JSONObject();

					NodeList authorProps = node.getChildNodes();
					for (int i = 0; i < authorProps.getLength(); i++) {
						Node prop = authorProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						authorJSon.put(label, value);
					}
					result.put("committer", authorJSon);
				}
				else if ("s3:commitFile".equals(nodeName)) {
					JSONObject file = new JSONObject();
					JSONArray modules = new JSONArray();
					
					NodeList fileProps = node.getChildNodes();
					for (int i = 0; i < fileProps.getLength(); i++) {
						Node fileProp = fileProps.item(i);
						String label = fileProp.getNodeName();
						
						if ("s3:fileId".equals(label))
							file.put("id", Long.parseLong(fileProp.getTextContent()));
						else if ("s3:fileUri".equals(label))
							file.put("uri", fileProp.getTextContent());
						else if ("s3:fileAction".equals(label))
							file.put("action", fileProp.getTextContent());
						else if ("s3:fileName".equals(label))
							file.put("name", fileProp.getTextContent());
						else if ("s3:fileBranch".equals(label))
							file.put("branch", node.getTextContent());
						else if ("s3:fileModules".equals(label)) {
							JSONObject module = new JSONObject();
							JSONArray methods = new JSONArray();
							
							NodeList moduleProps = fileProp.getChildNodes();
							for (int j = 0; j < moduleProps.getLength(); j++) {
								Node moduleProp = moduleProps.item(j);
								String moduleLabel = moduleProp.getNodeName();
								
								if ("s3:moduleUri".equals(moduleLabel))
									module.put("uri", moduleProp.getTextContent());
								else if ("s3:moduleId".equals(moduleLabel))
									module.put("id", Long.parseLong(moduleProp.getTextContent()));
								else if ("s3:moduleName".equals(moduleLabel))
									module.put("name", moduleProp.getTextContent());
								else if ("s3:moduleStartLine".equals(moduleLabel))
									module.put("startLine", Integer.parseInt(moduleProp.getTextContent()));
								else if ("s3:moduleEndLine".equals(moduleLabel))
									module.put("endLine", Integer.parseInt(moduleProp.getTextContent()));
								else if ("s3:moduleMethods".equals(moduleLabel)) {
									// parse the method
									JSONObject method = new JSONObject();
									
									NodeList methodProps = moduleProp.getChildNodes();
									for (int k = 0; k < methodProps.getLength(); k++) {
										Node methodProp = methodProps.item(k);
										String methodLabel = methodProp.getNodeName();
										
										if ("s3:methodId".equals(methodLabel))
											method.put("id", Long.parseLong(methodProp.getTextContent()));
										else if ("s3:methodStartLine".equals(methodLabel))
											method.put("startLine", Integer.parseInt(methodProp.getTextContent()));
										else if ("s3:methodEndLine".equals(methodLabel))
											method.put("endLine", Integer.parseInt(methodProp.getTextContent()));
										else 
											method.put(methodLabel.substring(3), methodProp.getTextContent());
									}
									
									methods.add(method);
								}
							}
							module.put("methods", methods);
							modules.add(module);
						} 
					}
					
					file.put("modules", modules);
					filesArr.add(file);
				}
				else if ("s3:commitProduct".equals(nodeName)) {
					JSONObject product = new JSONObject();
					
					NodeList prodProps = node.getChildNodes();
					for (int i = 0; i < prodProps.getLength(); i++) {
						Node prodProp = prodProps.item(i);
						String label = prodProp.getNodeName().substring(3);
						
						product.put(label, prodProp.getTextContent());
					}
				}
				
				
				result.put("files", filesArr);
			}
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing commit details response message!", t);
		}
	}
	
	/**
	 * Parses issue details response message.
	 * 
	 * @param responseMsg The message to parse.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	@SuppressWarnings("unchecked")
	public static String parseAPIIssueDetailsMsg(String responseMsg) throws ParserConfigurationException, SAXException, IOException {
		try {
			JSONObject result = new JSONObject();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes("UTF-8")));
			
			JSONArray commentsJSon = new JSONArray();
			JSONArray activitiesJSon = new JSONArray();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
			
			// parse assigned to
			Node dataEl = doc.getElementsByTagName("s3:responseData").item(0);
			NodeList propNodes = dataEl.getChildNodes();
			for (int nodeIdx = 0; nodeIdx < propNodes.getLength(); nodeIdx++) {
				Node node = propNodes.item(nodeIdx);
				String nodeName = node.getNodeName();
				
				if ("s3:issueDependsOnid".equals(nodeName))	// parse depends on
					result.put("dependsOnId", Utils.parseLong(node.getTextContent()));
				else if ("s3:issueDependsOnUri".equals(nodeName))
					result.put("dependsOnUri", node.getTextContent());
				else if ("s3:issueUrl".equals(nodeName))	// parse URL
					result.put("url", node.getTextContent());
				else if ("s3:issueBlocksid".equals(nodeName))
					result.put("blocksId", node.getTextContent());
				else if ("s3:issueBlocksUri".equals(nodeName))
					result.put("blocksUri", node.getTextContent());
				else if ("s3:issueResolution".equals(nodeName))
					result.put("resolution", node.getTextContent());
				else if ("s3:issueDateOpened".equals(nodeName))
					result.put("dateOpened", dateFormat.parse(node.getTextContent()).getTime());
				else if ("s3:issueDescription".equals(nodeName))
					result.put("description", Utils.escapeHtml(node.getTextContent()));
				else if ("s3:issueLastModified".equals(nodeName))
					result.put("lastModified", dateFormat.parse(node.getTextContent()).getTime());
				else if ("s3:issueStatus".equals(nodeName))
					result.put("status", node.getTextContent());
				else if ("s3:issueAssignedTo".equals(nodeName)) {
					NodeList assignedProps = node.getChildNodes();
					
					JSONObject assignedJSon = new JSONObject();
					for (int i = 0; i < assignedProps.getLength(); i++) {
						Node prop = assignedProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						assignedJSon.put(label, value);
					}
					result.put("assignedTo", assignedJSon);
				} 
				else if ("s3:issueProduct".equals(nodeName)) {
					JSONObject productJSon = new JSONObject();
					
					NodeList prodProps = node.getChildNodes();
					for (int i = 0; i < prodProps.getLength(); i++) {
						Node prop = prodProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						productJSon.put(label, value);
					}
					result.put("product", productJSon);
				} 
				else if ("s3:issueAuthor".equals(nodeName)) {
					JSONObject authorJSon = new JSONObject();
					
					NodeList authorProps = node.getChildNodes();
					for (int i = 0; i < authorProps.getLength(); i++) {
						Node prop = authorProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						authorJSon.put(label, value);
					}
					result.put("author", authorJSon);
				}
				else if ("s3:issueComputerSystem".equals(nodeName)) {
					JSONObject systemJSon = new JSONObject();
					
					NodeList systemProps = node.getChildNodes();
					for (int i = 0; i < systemProps.getLength(); i++) {
						Node prop = systemProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						systemJSon.put(label, value);
					}
					result.put("computerSystem", systemJSon);
				}
				else if ("s3:issueCCPerson".equals(nodeName)) {
					JSONObject ccJSon = new JSONObject();
					
					NodeList ccProps = node.getChildNodes();
					for (int i = 0; i < ccProps.getLength(); i++) {
						Node prop = ccProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						ccJSon.put(label, value);
					}
					result.put("cc", ccJSon);
				}
				else if ("s3:issueActivity".equals(nodeName)) {
					JSONObject activityJSon = new JSONObject();
					
					NodeList activityProps = node.getChildNodes();
					for (int j = 0; j < activityProps.getLength(); j++) {
						Node prop = activityProps.item(j);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						activityJSon.put(label, value);
					}
					
					activitiesJSon.add(activityJSon);
				}
				else if ("s3:issueComment".equals(nodeName)) {
					JSONObject commentJSon = new JSONObject();
					
					NodeList commentProps = node.getChildNodes();
					for (int j = 0; j < commentProps.getLength(); j++) {
						Node commentNode = commentProps.item(j);
						String label = commentNode.getNodeName().substring(3);
						
						if ("commentPerson".equals(label)) {
							// person also has properties
							Node person = commentNode;
							JSONObject personJSon = new JSONObject();
							
							NodeList personProps = person.getChildNodes();
							for (int k = 0; k < personProps.getLength(); k++) {
								Node personProp = personProps.item(k);
								String personLabel = personProp.getNodeName().substring(3);
								String personValue = personProp.getTextContent();
								
								personJSon.put(personLabel, personValue);
							}
							
							commentJSon.put("person", personJSon);
						} else if("commentDate".equals(label)) {
							commentJSon.put("commentDate", dateFormat.parse(commentNode.getTextContent()).getTime());
						} else if("commentText".equals(label)) {
							commentJSon.put(label, Utils.escapeHtml(commentNode.getTextContent()));
						} else {
							commentJSon.put(label, commentNode.getTextContent());
						}
					}
					
					commentsJSon.add(commentJSon);
				}
			}
			
			result.put("activities", activitiesJSon);
			result.put("comments", commentsJSon);		
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing issue details response message!", t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static String parseKEUIItemDetailsMsg(String responseMsg) {
		try {
			JSONObject result = new JSONObject();
			
			Map<Long, JSONObject> personH = new HashMap<Long, JSONObject>();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes("UTF-8")));
			
			Element results = (Element) doc.getElementsByTagName("results").item(0);
			
			// parse people
			NodeList peopleNodes = results.getElementsByTagName("person");
			for (int i = 0; i < peopleNodes.getLength(); i++) {
				Node personNode = peopleNodes.item(i);
				JSONObject person = new JSONObject();
				
				NamedNodeMap attributes = personNode.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					Node attribute = attributes.item(j);
					String attrName = attribute.getNodeName();
					
					if ("id".equals(attrName))
						person.put("id", Long.parseLong(attribute.getNodeValue()));
					else person.put(attrName, attribute.getNodeValue());
				}
				
				long id = (Long) person.get("id");
				personH.put(id, person);
			}
			
			// parse content
			Element item = (Element) results.getElementsByTagName("item").item(0);
			
			long id = Long.parseLong(item.getAttribute("id"));
			int type = Integer.parseInt(item.getAttribute("itemType"));
			long time = Long.parseLong(item.getAttribute("time")) - 11644473600000L;
			String entryId = item.getAttribute("entryId");
			long threadId = Long.parseLong(item.getAttribute("threadId"));
			int count = Integer.parseInt(item.getAttribute("count"));
			JSONObject from = personH.get(Long.parseLong(item.getAttribute("from")));
			JSONObject to = personH.get(Utils.parseLong(item.getAttribute("to")));
			String content = Utils.escapeHtml(item.getElementsByTagName("fullContent").item(0).getTextContent());
			String subject = Utils.escapeHtml(item.getElementsByTagName("subject").item(0).getTextContent());
			
			JSONArray tags = new JSONArray();
			String[] tagsV = item.getAttribute("tags").split(",");
			for (String tag : tagsV)
				tags.add(Long.parseLong(tag));
			
			result.put("id", id);
			result.put("type", type);
			result.put("time", time);
			result.put("entryID", entryId);
			result.put("threadID", threadId);
			result.put("count", count);
			result.put("from", from);
			result.put("to", to);
			result.put("content", content);
			result.put("subject", subject);
			result.put("tags", tags);
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing item details response message!", t);
		}
	}

	/**
	 * Parses KEUI people response message.
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String parseKEUIPeopleResponse(String responseMsg) {
		int minSize = 9;
		int maxSize = 22;
		
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes("UTF-8")));
			
			JSONObject result = new JSONObject();
			
			// parse all the people
			List<JSONObject> nodeV = new ArrayList<>();
			List<JSONObject> edgeV = new ArrayList<>();
			Map<Long, JSONObject> nodeH = new HashMap<>();
			
			NodeList personNodeList = doc.getElementsByTagName("person");
			for (int i = 0; i < personNodeList.getLength(); i++) {
				Element personNode = (Element) personNodeList.item(i);
				JSONObject personJSon = new JSONObject();
				
				long id = Long.parseLong(personNode.getAttribute("id"));
				
				personJSon.put("id", id);
				personJSon.put("email", personNode.getAttribute("account"));
				personJSon.put("label", personNode.getAttribute("name"));
				personJSon.put("neighbours", new JSONArray());
				
				nodeV.add(personJSon);
				nodeH.put(id, personJSon);
			}
			
			// parse the counts
			int maxCount = 0;
			String fromCountsStr = doc.getElementsByTagName("fromCounts").item(0).getTextContent();
			
			// avoid an exception
			if (fromCountsStr.startsWith("\n")) fromCountsStr = fromCountsStr.substring(1);
			String[] sentDataV = fromCountsStr.split(",");
			for (String sentData : sentDataV) {
				if (sentData.isEmpty()) continue;
				
				String[] idCountPr = sentData.split("-");
				long personId = Long.parseLong(idCountPr[0]);
				int count = Integer.parseInt(idCountPr[1]);
				
				if (count > maxCount) maxCount = count;
				if (nodeH.containsKey(personId)) {
					JSONObject personJSon = nodeH.get(personId);
					personJSon.put("count", count);
				}
			}
			
			// set the size
			for (JSONObject person : nodeV) {
				int count = person.containsKey("count") ? (Integer) person.get("count") : 0;
				int size = (int) (count == 0 ? minSize : Math.min(Math.max(Math.ceil(maxSize*Math.sqrt(count)/Math.sqrt(maxCount)), minSize), maxSize));
				person.put("size", size);
			}
			
			// from/to counts
			String fromToCountsStr = doc.getElementsByTagName("fromToCounts").item(0).getTextContent();
			if (fromToCountsStr.startsWith("\n")) fromToCountsStr = fromToCountsStr.substring(1);
			
			String[] fromToCountsStrV = fromToCountsStr.split(",");
			for (String fromToStr : fromToCountsStrV) {
				if (fromToStr.isEmpty()) continue;
				
				JSONObject edge = new JSONObject();
				
				String[] fromToTup = fromToStr.split("-");
				
				long source = Long.parseLong(fromToTup[0]);
				long target = Long.parseLong(fromToTup[1]);
				
				edge.put("source", source);
				edge.put("target", target);
				
				// data
				JSONObject data = new JSONObject();
				data.put("directed", true);
				data.put("count", Integer.parseInt(fromToTup[2]));
				
				edge.put("data", data);
				edgeV.add(edge);
				
				if (nodeH.containsKey(source)) {
					JSONArray neighbours = (JSONArray) nodeH.get(source).get("neighbours");
					neighbours.add(target);
				}
			}
			
			JSONArray edgesJSon = new JSONArray();
			edgesJSon.addAll(edgeV);
			
			result.put("type", "peopleData");
			result.put("nodeH", new JSONObject(nodeH));
			result.put("edges", edgesJSon);
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI people response!!", t);
		}
	}

	/**
	 * Parses KEUI timeline response message
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String parseKEUITimelineResponse(String responseMsg) {
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes("UTF-8")));
			
			NodeList monthEls = doc.getElementsByTagName("month");
			
			Map<String, List<Integer>> monthH = new HashMap<>();
			List<String> monthLabelV = new ArrayList<>(monthEls.getLength());
			for (int i = 0; i < monthEls.getLength(); i++) {
				Element month = (Element) monthEls.item(i);
				
				String label = month.getAttribute("val");
				
				List<Integer> days = new ArrayList<>(31);
				String[] dayStrV = month.getTextContent().split(",");
				for (String dayStr : dayStrV)
					days.add(Integer.parseInt(dayStr));
				
				monthH.put(label, days);
				monthLabelV.add(label);
			}

			Collections.sort(monthLabelV, MONTH_LABEL_COMPARATOR);
			
			// insert missing months
			for (int i = 0; i < monthLabelV.size(); i++) {
				if (i < monthLabelV.size() - 1) {
					String[] currentPr = monthLabelV.get(i).split("-");
					String[] nextPr = monthLabelV.get(i+1).split("-");
					
					int currentYear = Integer.parseInt(currentPr[0]);
					int nextYear = Integer.parseInt(nextPr[0]);
					int currentMonth = Integer.parseInt(currentPr[1]);
					int nextMonth = Integer.parseInt(nextPr[1]);
					
					String newLabel = null;
					if (currentYear == nextYear) {
						if (nextMonth > currentMonth + 1)
							newLabel = currentYear + "-" + (currentMonth + 1);
					} else if (currentYear == nextYear - 1) {
						if (currentMonth < 12)
							newLabel = currentYear + "-" + (currentMonth + 1);
						else if (nextMonth > 1)
							newLabel = nextYear + "-1";
					} else	// a whole year is missing
						newLabel = (currentYear + 1) + "-1";
					
					// insert missing values
					if (newLabel != null) {
						monthLabelV.add(i+1, newLabel);
						
						// missing days
						int monthIdx = Integer.parseInt(newLabel.split("-")[1]) - 1;
						int monthLength = MONTH_LENGTHS[monthIdx];
						
						List<Integer> days = new ArrayList<>(monthLength);
						for (int j = 0; j < monthLength; j++)
							days.add(0);
						monthH.put(newLabel, days);
					}
				}
			}
			
			// create pairs of type [time, posts]
			TimeZone zone = TimeZone.getTimeZone("UTC");
			Calendar calendar = Calendar.getInstance(zone);
			
			JSONArray allDays = new JSONArray();
			for (String month : monthLabelV) {
				String[] yearMonthPr = month.split("-");
				int year = Integer.parseInt(yearMonthPr[0]);
				int monthIdx = Integer.parseInt(yearMonthPr[1]) - 1;
				
				List<Integer> days = monthH.get(month);
				for (int dayIdx = 0; dayIdx < days.size(); dayIdx++) {
					calendar.clear();
					calendar.set(year, monthIdx, dayIdx);
					long time = calendar.getTime().getTime();
					
					JSONArray dayArray = new JSONArray();
					dayArray.add(time);
					dayArray.add(days.get(dayIdx));
					allDays.add(dayArray);
				}
			}
			
			JSONObject result = new JSONObject();
			
			result.put("type", "timelineData");
			result.put("days", allDays);
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI timeline response!!", t);
		}
	}

	/**
	 * Parses KEUI keywords response message.
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String parseKEUIKeywordsResponse(String responseMsg) {
		try {
			JSONArray kwsJSon = new JSONArray();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes("UTF-8")));
			
			Element resultsNode = (Element) doc.getElementsByTagName("results").item(0);
			
			NodeList kws = resultsNode.getElementsByTagName("kw");
			for (int i = 0; i < kws.getLength(); i++) {
				Element kw = (Element) kws.item(i);
				
				String word = kw.getAttribute("str");
				double weight = Double.parseDouble(kw.getAttribute("wgt"));
				
				JSONObject kwJSon = new JSONObject();
				kwJSon.put("text", word);
				kwJSon.put("weight", weight);
				
				kwsJSon.add(kwJSon);
			}
			
			JSONObject result = new JSONObject();
			result.put("type", "keywordData");
			result.put("data", kwsJSon);
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI keyword response!!", t);
		}
	}

	/**
	 * Parses KEUI items response message.
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String parseKEUIItemsResponse(String responseMsg) {
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes("UTF-8")));
			
			JSONArray items = new JSONArray();
			
			// parse people
			Map<Long, JSONObject> peopleH = parsePeopleH(doc.getElementsByTagName("person"));
			
			Element infoEl = (Element) doc.getElementsByTagName("info").item(0);
			int totalCount = Integer.parseInt(infoEl.getAttribute("totalCount"));
			int offset = Integer.parseInt(infoEl.getAttribute("offset"));
			int limit = Integer.parseInt(infoEl.getAttribute("maxCount"));
			
			JSONObject info = new JSONObject();
			info.put("totalCount", totalCount);
			info.put("offset", offset);
			info.put("limit", limit);
			
			List<String> keywords = new ArrayList<>();
			Element keywordsEl = doc.getElementsByTagName("enrychableKeywords").getLength() > 0 ? (Element) doc.getElementsByTagName("enrychableKeywords").item(0) : null;
			if (keywordsEl != null)
				keywords.addAll(Arrays.asList(keywordsEl.getTextContent().replaceAll("[\\+-]", "").split("[\\s+,-]")));
			
			
			NodeList itemNodes = doc.getElementsByTagName("item");
			for (int i = 0; i < itemNodes.getLength(); i++) {
				Element itemNode = (Element) itemNodes.item(i);
				JSONObject itemJSon = parseKEUIItem(itemNode);
				if (itemJSon == null) continue;
				
				// emphasize the keywords
				if (!keywords.isEmpty()) {
					String content = (String) itemJSon.get("content");
					for (String keyword : keywords) {
						content = content.replaceAll("(?i)(" + keyword + ")", "<em>$1</em>");
					}
					itemJSon.put("content", content);
					if (itemJSon.containsKey("subject")) {
						String subject = (String) itemJSon.get("subject");
						for (String keyword : keywords)
							subject = subject.replaceAll("(?i)(" + keyword + ")", "<em>$1</em>");
						itemJSon.put("subject", subject);
					}
					
				}
				
				items.add(itemJSon);
			}
			
			JSONObject result = new JSONObject();
			result.put("type", "itemData");
			result.put("persons", new JSONObject(peopleH));
			result.put("items", items);
			result.put("info", info);
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI items response!!", t);
		}
	}
	
	/**
	 * Parses KEUI duplicate issue response.
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String parseKEUIDuplicateResponse(String responseMsg) {
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes("UTF-8")));
			
			Element results = (Element) doc.getElementsByTagName("results").item(0);
			
			JSONArray items = new JSONArray();
			Map<Long, JSONObject> peopleH = parsePeopleH(results.getElementsByTagName("person"));
			
			NodeList itemNodes = results.getElementsByTagName("item");
			for (int i = 0; i < itemNodes.getLength(); i++) {
				Element node = (Element) itemNodes.item(i);
				
				JSONObject item = parseKEUIItem(node);
				if (item != null)
					items.add(item);
			}
			
			JSONObject info = new JSONObject();
			info.put("totalCount", Double.POSITIVE_INFINITY);
			info.put("offset", 0);
			info.put("limit", items.size());
			
			JSONObject result = new JSONObject();
			result.put("type", "itemData");
			result.put("persons", new JSONObject(peopleH));
			result.put("items", items);
			result.put("info", info);
			
			return result.toJSONString();
		} catch(Throwable t) {
			throw new IllegalArgumentException("An exception occurred while parsing duplicate issue response!", t);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void parseKEUIMetadata(Element node, JSONObject result) {
		if (node.getElementsByTagName("metaData").getLength() > 0) {
			Element metadata = (Element) node.getElementsByTagName("metaData").item(0);
			
			if (metadata.getElementsByTagName("issueId").getLength() > 0)
				result.put("issueID", Long.parseLong(metadata.getElementsByTagName("issueId").item(0).getTextContent()));
			if (metadata.getElementsByTagName("url").getLength() > 0)
				result.put("url", metadata.getElementsByTagName("url").item(0).getTextContent());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static JSONObject parseKEUIItem(Element node) {
		JSONObject result = new JSONObject();
		
		long id = Long.parseLong(node.getAttribute("id"));
		int  type = Integer.parseInt(node.getAttribute("itemType"));
		long time = Long.parseLong(node.getAttribute("time")) - 11644473600000L;
		String[] tags = node.getAttribute("tags").split(",");
		String entryId = node.getAttribute("entryId");
		String content = Utils.escapeHtml(node.getElementsByTagName("shortContent").item(0).getTextContent());
		
		JSONArray tagsArr = new JSONArray();
		tagsArr.addAll(Arrays.asList(tags));
		
		if (type == ItemType.BUG_DESCRIPTION.value) {
			Long authorId = node.getAttribute("author").isEmpty() ? null : Long.parseLong(node.getAttribute("author"));
			int count = Integer.parseInt(node.getAttribute("count"));
			long threadId = Long.parseLong(node.getAttribute("threadId"));
			String subject = Utils.escapeHtml(node.getElementsByTagName("subject").item(0).getTextContent());
			
			if (node.getElementsByTagName("similarity").item(0) != null)
				result.put("similarity", Double.parseDouble(node.getElementsByTagName("similarity").item(0).getTextContent()));
			
			parseKEUIMetadata(node, result);
			
			result.put("authorID", authorId);
			result.put("count", count);
			result.put("content", content);
			result.put("entryID", entryId);
			result.put("id", id);
			result.put("subject", subject);
			result.put("tags", tagsArr);
			result.put("threadID", threadId);
			result.put("time", time);
			result.put("type", type);
		}
		else if (type == ItemType.COMMIT.value) {
			long authorId = Long.parseLong(node.getAttribute("author"));
			result.put("id", id);
			result.put("type", type);
			result.put("time", time);
			result.put("tags", tagsArr);
			result.put("entryID", entryId);
			result.put("authorID", authorId);
			result.put("content", content);
		} 
		else if(type == ItemType.EMAIL.value || type == ItemType.POST.value || 
				type == ItemType.BUG_COMMENT.value || type == ItemType.WIKI_POST.value) {
			long threadId = Long.parseLong(node.getAttribute("threadId"));
			int count = Integer.parseInt(node.getAttribute("count"));
			long senderId = Long.parseLong(node.getAttribute("from"));
			String url = node.getElementsByTagName("url").getLength() > 0 ? node.getElementsByTagName("url").item(0).getTextContent() : null;
			
			String subject = Utils.escapeHtml(node.getElementsByTagName("subject").item(0).getTextContent());
			
			// recipients
			JSONArray recipients = new JSONArray();
			String recipientsStr = node.getAttribute("to");
			if (recipientsStr != null && !recipientsStr.isEmpty()) {
				String[] recipientStrV = recipientsStr.split(",");
				for (String recIdStr : recipientStrV)
					recipients.add(Long.parseLong(recIdStr));
			}
			
			parseKEUIMetadata(node, result);	
			
			result.put("id", id);
			result.put("type", type);
			result.put("time", time);
			result.put("tags", tagsArr);
			result.put("entryID", entryId);
			result.put("threadID", threadId);
			result.put("count", count);
			result.put("senderID", senderId);
			result.put("recipientIDs", recipients);
			result.put("content", content);
			result.put("subject", subject);
			result.put("url", url);
		}
		else {
			log.warn("Unknown item type received: " + type + ", ignoring...");
			return null;
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static Map<Long, JSONObject> parsePeopleH(NodeList peopleNodes) {
		Map<Long, JSONObject> result = new HashMap<>();
		for (int i = 0; i < peopleNodes.getLength(); i++) {
			Element node = (Element) peopleNodes.item(i);
			JSONObject person = new JSONObject();
			
			long id = Long.parseLong(node.getAttribute("id"));
			
			person.put("id", id);
			person.put("account", node.getAttribute("account"));
			person.put("name", node.getAttribute("name"));
			result.put(id, person);
		}
		
		return result;
	}

	/**
	 * Parses a KEUI suggestion message and returns a JSON <code>String</code>.
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String parseKEUISuggestMessage(String responseMsg) {
		try {
			// parse the suggestions and return JSON
			JSONArray jsonArray = new JSONArray();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document xmlDoc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes("UTF-8")));
			
			NodeList suggRootNodes = xmlDoc.getElementsByTagName("suggestions");
			if (suggRootNodes.getLength() != 1)
				throw new IllegalArgumentException("The suggestion message has 0 or more then 1 suggestion nodes!");
			
			Node suggRoot = suggRootNodes.item(0);
			NodeList suggNodes = suggRoot.getChildNodes();
			for (int i = 0; i < suggNodes.getLength(); i++) {
				Node suggNode = suggNodes.item(i);
				NamedNodeMap attributes = suggNode.getAttributes();
				
				JSONObject jsonObj = new JSONObject();
				
				String tagName = suggNode.getNodeName();
				String label;
				String value;
				String type;
				
				switch (tagName) {
				case "person":
					label = attributes.getNamedItem("name").getNodeValue();
					value = attributes.getNamedItem("account").getNodeValue();
					type = "person";
					break;
				case "concept":
					label = attributes.getNamedItem("label").getNodeValue();
					value = attributes.getNamedItem("uri").getNodeValue();
					type = "concept";
					break;
				case "product":
					label = attributes.getNamedItem("label").getNodeValue();
					value = attributes.getNamedItem("uri").getNodeValue();
					type = "product";
					break;
				case "issue":
					label = attributes.getNamedItem("label").getNodeValue();
					value = label;
					type = "issue";
					break;
				case "file":
					String fullName = attributes.getNamedItem("name").getNodeValue();
					
					label = Utils.getFileName(fullName);
					value = attributes.getNamedItem("uri").getNodeValue();
					type = "source";
					
					jsonObj.put("path", Utils.getFilePath(fullName));
					jsonObj.put("tooltip", Utils.escapeHtml(attributes.getNamedItem("tooltip").getNodeValue()));
					break;
				case "module":
					label = attributes.getNamedItem("name").getNodeValue();
					value = attributes.getNamedItem("uri").getNodeValue();
					type = "source";
					
					String fullPath = Utils.escapeHtml(attributes.getNamedItem("tooltip").getNodeValue());
					jsonObj.put("path", fullPath);
					jsonObj.put("tooltip", fullPath);
					break;
				case "method":
					label = attributes.getNamedItem("name").getNodeValue();
					value = attributes.getNamedItem("uri").getNodeValue();
					type = "source";
					
					String fullPath1 = Utils.escapeHtml(attributes.getNamedItem("tooltip").getNodeValue());
					jsonObj.put("path", fullPath1);
					jsonObj.put("tooltip", fullPath1);
					break;
				default:
					log.warn("Invalid suggestion tag: " + tagName + ", ignoring...");
					continue;
				}
				
			
				jsonObj.put("label", Utils.escapeHtml(label));
				jsonObj.put("value", Utils.escapeHtml(value));
				jsonObj.put("type", type);
				
				jsonArray.add(jsonObj);
			}
			
			return jsonArray.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI suggestion response!!", t);
		}
	}

	/**
	 * Parses Recommenders ALERT.Recommender.IssueRecommendation.xml message to extract issue IDs.
	 * 
	 * @param msg
	 * @return
	 */
	public static List<Long> parseRecommenderIssueIdsMsg(String msg) {
		try {
			List<Long> result = new ArrayList<>();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document xmlDoc = builder.parse(new ByteArrayInputStream(msg.getBytes("UTF-8")));
			
			Element eventData = (Element) xmlDoc.getElementsByTagName("ns1:eventData").item(0);
			NodeList issues = eventData.getElementsByTagName("sc:issue");
			
			for (int i = 0; i < issues.getLength(); i++) {
				Element issue = (Element) issues.item(i);
				
				Element idEl = (Element) issue.getElementsByTagName("sc:id").item(0);
				Long id = Long.parseLong(idEl.getTextContent());
				result.add(id);
			}
			
			return result;
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing Recommender IssueRecommendation.xml message!", t);
		}
	}

	/**
	 * Parses the notification feed and returns a list of notifications.
	 * 
	 * @param response
	 * @return
	 */
	public static List<Notification> parseNotificationRSS(String feed) {
		try {
			List<Notification> result = new ArrayList<>();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document xmlDoc = builder.parse(new ByteArrayInputStream(feed.getBytes("UTF-8")));
			
			Element channel = (Element) xmlDoc.getElementsByTagName("channel").item(0);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
			
			NodeList fields = channel.getChildNodes();
			for (int i = 0; i < fields.getLength(); i++) {
				Node node = fields.item(i);
				String tag = node.getNodeName();
				
				if ("item".equals(tag)) {
					Element item = (Element) node;
					Notification notification;
					
					// first get all the common attributes
					// watch out, these fields may be null
					String title = null;
					String link = null;
					Date pubDate = null;
					
					NodeList titles = item.getElementsByTagName("title");
					NodeList links = item.getElementsByTagName("link");
					NodeList dates = item.getElementsByTagName("pubDate");
					
					if (titles.getLength() > 0) title = titles.item(0).getTextContent();
					if (links.getLength() > 0) link = links.item(0).getTextContent();
					if (dates.getLength() > 0) pubDate = dateFormat.parse(dates.item(0).getTextContent());
					
					// get the specialized attributes
					Element content = (Element) item.getElementsByTagName("content:encoded").item(0);
					// content has only 1 child, I can do a switch on it's name
					
					Node data = content.getChildNodes().item(0);
					switch (data.getNodeName()) {
					case "issue":
						notification = parseIssueNotification(data);
						break;
					case "event":
						notification = parseEventNotification(data);
						break;
					case "identity":
						notification = parseIdentityNotification(data);
						break;
					case "item":
						notification = parseItemNotification(data);
						break;
					default:
						log.warn("Unknown notification type: " + data.getNodeName());
						continue;
					}
					
					notification.setTitle(title);
					notification.setLink(link);
					notification.setPublishDate(pubDate);
					
					result.add(notification);
				}
			}
			return result;
		} catch (Throwable t) {
			throw new IllegalArgumentException("Failed to parse notification feed!", t);
		}
	}

	/**
	 * Parses similar item notification.
	 * 
	 * @param itemEl
	 * @return
	 */
	private static Notification parseItemNotification(Node itemEl) {
		ItemNotification result = new ItemNotification();
		
		NodeList propNodes = itemEl.getChildNodes();
		for (int i = 0; i < propNodes.getLength(); i++) {
			Element node = (Element) propNodes.item(i);
			String label = node.getNodeName();
			
			switch (label) {
			case "url":
				result.setUrl(node.getTextContent());
				break;
			case "similarity":
				result.setSimilarity(Double.parseDouble(node.getTextContent()));
				break;
			case "shortContent":
				result.setContent(Utils.escapeHtml(node.getTextContent()));
				break;
			case "subject":
				result.setSubject(Utils.escapeHtml(node.getTextContent()));
				break;
			default:
				log.warn("Unknown property of item notification: " + label + ", ignoring...");
			}
		}
		
		return result;
	}

	/**
	 * Parses identity notification.
	 * 
	 * @param identityEl
	 * @return
	 */
	private static Notification parseIdentityNotification(Node identityEl) {
		IdentityNotification result = new IdentityNotification();
		
		NodeList props = identityEl.getChildNodes();
		for (int i = 0; i < props.getLength(); i++) {
			Element node = (Element) props.item(i);
			String label = node.getNodeName();
			
			switch (label) {
			case "name":
				result.setName(node.getTextContent());
				break;
			case "profile":
				NodeList profileUrlList = node.getElementsByTagName("url");
				if (profileUrlList.getLength() > 0)
					result.setProfileUrl(profileUrlList.item(0).getTextContent());
				
				result.setProfile(node.getTextContent());
				break;
			case "imgurl":
				result.setImageUrl(node.getTextContent());
				break;
			default:
				log.warn("Unknown property of identity notification: " + label + ", ignoring...");
			}
		}
		
		return result;
	}

	/**
	 * Parses event notification.
	 * 
	 * @param eventEl
	 * @return
	 */
	private static Notification parseEventNotification(Node eventEl) {
		EventNotification result = new EventNotification();
		
		NodeList props = eventEl.getChildNodes();
		for (int i = 0; i < props.getLength(); i++) {
			Element node = (Element) props.item(i);
			String label = node.getNodeName();
			
			switch (label) {
			case "name":
				result.setName(node.getTextContent());
				break;
			case "description":
				result.setDescription(node.getTextContent());
				break;
			case "url":
				result.setUrl(node.getTextContent());
				break;
			default:
				log.warn("Unknown property of event notification: " + label + ", ignoring...");
			}
		}
		
		return result;
	}

	/**
	 * Parses issue notification.
	 * 
	 * @param eventEl
	 * @return
	 */
	private static Notification parseIssueNotification(Node issueEl) {
		IssueNotification result = new IssueNotification();
		
		NodeList props = issueEl.getChildNodes();
		for (int i = 0; i < props.getLength(); i++) {
			Element node = (Element) props.item(i);
			String label = node.getNodeName();
		
			switch (label) {
			case "bugid":
				result.setBugId(node.getTextContent());
				break;
			case "subject":
				result.setSubject(Utils.escapeHtml(node.getTextContent()));
				break;
			case "summary":
				result.setSummary(Utils.escapeHtml(node.getTextContent()));
				break;
			case "url":
				result.setUrl(node.getTextContent());
				break;
			default:
				log.warn("Unknown property of issue notification: " + label + ", ignoring...");
			}
		}
		
		return result;
	}
}
