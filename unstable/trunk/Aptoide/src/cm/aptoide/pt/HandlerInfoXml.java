/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt;

import cm.aptoide.pt.util.Md5Handler;
import cm.aptoide.pt.views.ViewApkInfoXml;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class HandlerInfoXml extends DefaultHandler {

    private boolean multipleApk;

    interface ElementHandler {
		void startElement(Attributes atts) throws SAXException;
		void endElement() throws SAXException;
	}

	final Map<String, ElementHandler> elements = new HashMap<String, ElementHandler>();

	void loadElements() {

		elements.put("apklst", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {

			}
		});

        elements.put("localize", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {
            }

            @Override
            public void endElement() throws SAXException {
                apk.getServer().coutriesPermitted = new ArrayList<String>(Arrays.asList(sb.toString().split(",")));
            }
        });

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

		elements.put("repository", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				if(!delta){
					db.deleteServer(apk.getServer().id,false);
				}
				db.insertServerInfo(apk.getServer(),Category.INFOXML);

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

		elements.put("del", new ElementHandler() {


			public void startElement(Attributes atts) throws SAXException {
				isRemove = true;
			}

			@Override
			public void endElement() throws SAXException {
				isRemove = true;
			}
		});

		elements.put("basepath", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.getServer().basePath = sb.toString();
			}
		});

		elements.put("appscount", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {

			}
		});

		elements.put("iconspath", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.getServer().iconsPath = sb.toString();
			}
		});

		elements.put("screenspath", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.getServer().screenspath=sb.toString();
			}
		});

		elements.put("webservicespath", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.getServer().webservicesPath=sb.toString();
			}
		});

//		elements.put("apkpath", new ElementHandler() {
//			public void startElement(Attributes atts) throws SAXException {
//
//			}
//
//			@Override
//			public void endElement() throws SAXException {
//				apk.getServer().apkPath=sb.toString();
//			}
//		});

		elements.put("package", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {
				apk.clear();
			}

			@Override
			public void endElement() throws SAXException {
                if (!multipleApk) {
                    if (isRemove) {
                        db.remove(apk, apk.getServer());
                        isRemove = false;
                    } else {
                        db.insert(apk);
                    }
                }else{
                    multipleApk = false;
                }

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

		elements.put("name", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setName(sb.toString());
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

		elements.put("vercode", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setVercode(Integer.parseInt(sb.toString()));

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

		elements.put("catg", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setCategory1(sb.toString());
			}
		});

		elements.put("catg2", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setCategory2(sb.toString());
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

		elements.put("minSdk", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setMinSdk(sb.toString());
			}
		});

		elements.put("delta", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				delta = true;
				if(sb.toString().length()>0){
					apk.getServer().hash = sb.toString();
				}

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

		elements.put("minGles", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
				apk.setMinGlEs(sb.toString());
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

        elements.put("price", new ElementHandler() {


            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setPrice(Double.parseDouble(sb.toString()));

            }
        });

	}

	private String path;

	public HandlerInfoXml(Server server, String path) {
		this.path = path;

		apk.setServer(server);

		loadElements();
	}

	StringBuilder sb = new StringBuilder();
	ViewApkInfoXml apk = new ViewApkInfoXml();
	static Database db = Database.getInstance();

	private boolean isRemove = false;

	long start;

	private boolean delta = false;

	public void startDocument() throws SAXException {
		start = System.currentTimeMillis();
		db.prepare();
		apk.setRepo_id(apk.getServer().id);
		delta = false;

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
		}
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		if(!delta ){
			System.out.println("Writing delta");
			apk.getServer().hash = Md5Handler.md5Calc(new File(path));
			System.out.println("Delta is:" +apk.getServer().hash);
            db.updateDelta(apk.getServer());
		}
	}

}
