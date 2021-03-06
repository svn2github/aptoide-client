/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt;

import android.content.ContentValues;
import cm.aptoide.pt.util.Utils;
import cm.aptoide.pt.views.ViewApkLatest;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class HandlerLatest extends DefaultHandler{

    private boolean multipleApk = false;

    interface ElementHandler {
		void startElement(Attributes atts) throws SAXException;
		void endElement() throws SAXException;
	}

	Map<String, ElementHandler> elements = new HashMap<String, ElementHandler>();
	ViewApkLatest apk;
	StringBuilder sb  = new StringBuilder();
	boolean insidePackage = false;

	void loadElements() {

        elements.put("cpu", new ElementHandler() {
            @Override
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setCpuAbi(sb.toString());
            }
        });

        elements.put("screenCompat", new ElementHandler() {
            @Override
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setScreenCompat(sb.toString());
            }
        });

		elements.put("name", new ElementHandler() {


			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				if(insidePackage){
					apk.setName(sb.toString());
				}else{
					server.name=sb.toString();
				}
			}
		});

        elements.put(Utils.getMyCountry(ApplicationAptoide.getContext()).toUpperCase(Locale.ENGLISH), new ElementHandler() {


            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setName(sb.toString());
            }
        });

		elements.put("screenspath", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				server.screenspath=sb.toString();
			}
		});

		elements.put("repository", new ElementHandler() {


			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {

//				if(!db.getRepoHash(server.id,Category.LATEST).equals(server.hash)){
//					System.out.println("Deleting " +Category.LATEST.name() +" apps ");
//					db.deleteTopOrLatest(server.id,Category.LATEST);
//				}else{
//					System.out.println("NOT Deleting " +Category.LATEST.name() +" apps ");
//					throw new SAXException();
//				}
				db.insertServerInfo(server, Category.LATEST);
			}
		});

		elements.put("date", new ElementHandler() {


			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setDate(sb.toString());
			}
		});

		elements.put("hash", new ElementHandler() {


			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {

			}
		});

		elements.put("package", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {
				apk.clear();
				insidePackage = true;

			}

			@Override
			public void endElement() throws SAXException {
//				apk.setId(

                if(!multipleApk){

						db.insert(apk);
//						);
                }else{
                    multipleApk = false;
                }
//				db.insertScreenshots(apk,category);
				insidePackage = false;
			}
		});



        elements.put("multipleapk", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {
                multipleApk = true;
            }

            @Override
            public void endElement() throws SAXException {

            }
        });

        elements.put("apk", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                db.insert(apk);
            }
        });

		elements.put("ver", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setVername(sb.toString());
			}
		});

		elements.put("apkid", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setApkid(sb.toString());
			}
		});

		elements.put("vercode", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setVercode(Integer.parseInt(sb.toString()));
			}
		});



		elements.put("screen", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.addScreenshot(sb.toString());
			}
		});

		elements.put("icon", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setIconPath(sb.toString());
			}
		});

		elements.put("dwn", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setDownloads(sb.toString());
			}
		});

		elements.put("rat", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setRating(sb.toString());
			}
		});


		elements.put("basepath", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				server.basePath=sb.toString();
			}
		});

		elements.put("iconspath", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				server.iconsPath=sb.toString();
			}
		});

		elements.put("minSdk", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setMinSdk(sb.toString());
			}
		});

		elements.put("minGles", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setMinGlEs(sb.toString());
			}
		});

		elements.put("minScreen", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setMinScreen(Filters.Screens.lookup(sb.toString()).ordinal());
			}
		});

		elements.put("age", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setAge(Filters.Ages.lookup(sb.toString()).ordinal());
			}
		});

		elements.put("cmt", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				value = new ContentValues();
				value.put(ExtrasDbOpenHelper.COLUMN_COMMENTS_APKID, apk.getApkid());
				value.put(ExtrasDbOpenHelper.COLUMN_COMMENTS_COMMENT, sb.toString());
				values.add(value);
				i++;
				if(i%100==0){
					Database.context.getContentResolver().bulkInsert(ExtrasContentProvider.CONTENT_URI, values.toArray(value2));
					values.clear();
				}
			}
		});


	}

	private static Database db = Database.getInstance();
	private Server server;

	public HandlerLatest(Server server) {
		loadElements();
		this.server=server;
		apk = new ViewApkLatest();
		apk.setServer(server);
		apk.setRepo_id(server.id);
	}
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();

	}
	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attributes)
			throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		sb.setLength(0);
		ElementHandler elementHandler = elements.get(localName);

		if (elementHandler != null) {
			elementHandler.startElement(attributes);
		} else {
//			System.out.println("Element not found:" + localName);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		sb.append(ch,start,length);
	}

	@Override
	public void endElement(String uri, String localName,
			String qName) throws SAXException {
		super.endElement(uri, localName, qName);

		ElementHandler elementHandler = elements.get(localName);

		if (elementHandler != null) {
			elementHandler.endElement();
		} else {
//			System.out.println("Element not found:" + localName);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
new Thread(new Runnable() {

			@Override
			public void run() {
				if(values.size()>0){
					Database.context.getContentResolver().bulkInsert(ExtrasContentProvider.CONTENT_URI, values.toArray(value2));
					values.clear();
				}
			}
		}).start();
//		db.endTransation(server);
	}
	private static int i = 0;
	private static ContentValues value;
	private static ContentValues[] value2 = new ContentValues[0];
	private static ArrayList<ContentValues> values = new ArrayList<ContentValues>();


}
