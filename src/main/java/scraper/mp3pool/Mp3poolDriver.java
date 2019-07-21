package scraper.mp3pool;

import configuration.YamlConfig;
import mongodb.MongoControl;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import utils.CheckDate;
import utils.Constants;
import utils.Logger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Mp3poolDriver {

    private static String USERNAME;
    private static String PASS;
    private WebDriver scrapeDriver;
    static MongoControl mongoControl = new MongoControl();
    private Mp3PoolScraper mp3PoolScraper = new Mp3PoolScraper();
    static Logger mp3Logger = new Logger("Mp3Pool");

    public Mp3poolDriver() {
        String pathToSelenium = Constants.filesDir + "geckodriver.exe";
        System.setProperty("webdriver.gecko.driver", pathToSelenium);
        YamlConfig yamlConfig = new YamlConfig();

        USERNAME = yamlConfig.config.getMp3_pool_username();
        PASS = yamlConfig.config.getMp3_pool_password();
    }

    public static void main(String[] args) {
        Mp3poolDriver mp3poolDriver = new Mp3poolDriver();
        mp3poolDriver.test();
    }

    private void login(WebDriver driver) {
        mp3Logger.log("Login");
        driver.get("https://mp3poolonline.com/user/login");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        // Enter Username
        WebElement nameField = driver.findElement(By.id("edit-name"));
        nameField.sendKeys(USERNAME);
        // Enter Password
        WebElement passwordField = driver.findElement(By.id("edit-pass"));
        passwordField.sendKeys(PASS);
        // Click Login
        driver.findElement(By.id("edit-submit")).click();
    }

    private WebDriver getFirefoxDriver(String downloadPath) {
        FirefoxProfile fxProfile = new FirefoxProfile();
        fxProfile.setPreference("browser.download.folderList", 2);
        fxProfile.setPreference("browser.download.manager.showWhenStarting", false);
        fxProfile.setPreference("browser.download.dir", downloadPath);
        fxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/vnd.hzn-3d-crossword;video/3gpp;video/3gpp2;application/vnd.mseq;application/vnd.3m.post-it-notes;application/vnd.3gpp.pic-bw-large;application/vnd.3gpp.pic-bw-small;application/vnd.3gpp.pic-bw-var;application/vnd.3gp2.tcap;application/x-7z-compressed;application/x-abiword;application/x-ace-compressed;application/vnd.americandynamics.acc;application/vnd.acucobol;application/vnd.acucorp;audio/adpcm;application/x-authorware-bin;application/x-athorware-map;application/x-authorware-seg;application/vnd.adobe.air-application-installer-package+zip;application/x-shockwave-flash;application/vnd.adobe.fxp;application/pdf;application/vnd.cups-ppd;application/x-director;applicaion/vnd.adobe.xdp+xml;application/vnd.adobe.xfdf;audio/x-aac;application/vnd.ahead.space;application/vnd.airzip.filesecure.azf;application/vnd.airzip.filesecure.azs;application/vnd.amazon.ebook;application/vnd.amiga.ami;applicatin/andrew-inset;application/vnd.android.package-archive;application/vnd.anser-web-certificate-issue-initiation;application/vnd.anser-web-funds-transfer-initiation;application/vnd.antix.game-component;application/vnd.apple.installe+xml;application/applixware;application/vnd.hhe.lesson-player;application/vnd.aristanetworks.swi;text/x-asm;application/atomcat+xml;application/atomsvc+xml;application/atom+xml;application/pkix-attr-cert;audio/x-aiff;video/x-msvieo;application/vnd.audiograph;image/vnd.dxf;model/vnd.dwf;text/plain-bas;application/x-bcpio;application/octet-stream;image/bmp;application/x-bittorrent;application/vnd.rim.cod;application/vnd.blueice.multipass;application/vnd.bm;application/x-sh;image/prs.btif;application/vnd.businessobjects;application/x-bzip;application/x-bzip2;application/x-csh;text/x-c;application/vnd.chemdraw+xml;text/css;chemical/x-cdx;chemical/x-cml;chemical/x-csml;application/vn.contact.cmsg;application/vnd.claymore;application/vnd.clonk.c4group;image/vnd.dvb.subtitle;application/cdmi-capability;application/cdmi-container;application/cdmi-domain;application/cdmi-object;application/cdmi-queue;applicationvnd.cluetrust.cartomobile-config;application/vnd.cluetrust.cartomobile-config-pkg;image/x-cmu-raster;model/vnd.collada+xml;text/csv;application/mac-compactpro;application/vnd.wap.wmlc;image/cgm;x-conference/x-cooltalk;image/x-cmx;application/vnd.xara;application/vnd.cosmocaller;application/x-cpio;application/vnd.crick.clicker;application/vnd.crick.clicker.keyboard;application/vnd.crick.clicker.palette;application/vnd.crick.clicker.template;application/vn.crick.clicker.wordbank;application/vnd.criticaltools.wbs+xml;application/vnd.rig.cryptonote;chemical/x-cif;chemical/x-cmdf;application/cu-seeme;application/prs.cww;text/vnd.curl;text/vnd.curl.dcurl;text/vnd.curl.mcurl;text/vnd.crl.scurl;application/vnd.curl.car;application/vnd.curl.pcurl;application/vnd.yellowriver-custom-menu;application/dssc+der;application/dssc+xml;application/x-debian-package;audio/vnd.dece.audio;image/vnd.dece.graphic;video/vnd.dec.hd;video/vnd.dece.mobile;video/vnd.uvvu.mp4;video/vnd.dece.pd;video/vnd.dece.sd;video/vnd.dece.video;application/x-dvi;application/vnd.fdsn.seed;application/x-dtbook+xml;application/x-dtbresource+xml;application/vnd.dvb.ait;applcation/vnd.dvb.service;audio/vnd.digital-winds;image/vnd.djvu;application/xml-dtd;application/vnd.dolby.mlp;application/x-doom;application/vnd.dpgraph;audio/vnd.dra;application/vnd.dreamfactory;audio/vnd.dts;audio/vnd.dts.hd;imag/vnd.dwg;application/vnd.dynageo;application/ecmascript;application/vnd.ecowin.chart;image/vnd.fujixerox.edmics-mmr;image/vnd.fujixerox.edmics-rlc;application/exi;application/vnd.proteus.magazine;application/epub+zip;message/rfc82;application/vnd.enliven;application/vnd.is-xpr;image/vnd.xiff;application/vnd.xfdl;application/emma+xml;application/vnd.ezpix-album;application/vnd.ezpix-package;image/vnd.fst;video/vnd.fvt;image/vnd.fastbidsheet;application/vn.denovo.fcselayout-link;video/x-f4v;video/x-flv;image/vnd.fpx;image/vnd.net-fpx;text/vnd.fmi.flexstor;video/x-fli;application/vnd.fluxtime.clip;application/vnd.fdf;text/x-fortran;application/vnd.mif;application/vnd.framemaker;imae/x-freehand;application/vnd.fsc.weblaunch;application/vnd.frogans.fnc;application/vnd.frogans.ltf;application/vnd.fujixerox.ddd;application/vnd.fujixerox.docuworks;application/vnd.fujixerox.docuworks.binder;application/vnd.fujitu.oasys;application/vnd.fujitsu.oasys2;application/vnd.fujitsu.oasys3;application/vnd.fujitsu.oasysgp;application/vnd.fujitsu.oasysprs;application/x-futuresplash;application/vnd.fuzzysheet;image/g3fax;application/vnd.gmx;model/vn.gtw;application/vnd.genomatix.tuxedo;application/vnd.geogebra.file;application/vnd.geogebra.tool;model/vnd.gdl;application/vnd.geometry-explorer;application/vnd.geonext;application/vnd.geoplan;application/vnd.geospace;applicatio/x-font-ghostscript;application/x-font-bdf;application/x-gtar;application/x-texinfo;application/x-gnumeric;application/vnd.google-earth.kml+xml;application/vnd.google-earth.kmz;application/vnd.grafeq;image/gif;text/vnd.graphviz;aplication/vnd.groove-account;application/vnd.groove-help;application/vnd.groove-identity-message;application/vnd.groove-injector;application/vnd.groove-tool-message;application/vnd.groove-tool-template;application/vnd.groove-vcar;video/h261;video/h263;video/h264;application/vnd.hp-hpid;application/vnd.hp-hps;application/x-hdf;audio/vnd.rip;application/vnd.hbci;application/vnd.hp-jlyt;application/vnd.hp-pcl;application/vnd.hp-hpgl;application/vnd.yamaha.h-script;application/vnd.yamaha.hv-dic;application/vnd.yamaha.hv-voice;application/vnd.hydrostatix.sof-data;application/hyperstudio;application/vnd.hal+xml;text/html;application/vnd.ibm.rights-management;application/vnd.ibm.securecontainer;text/calendar;application/vnd.iccprofile;image/x-icon;application/vnd.igloader;image/ief;application/vnd.immervision-ivp;application/vnd.immervision-ivu;application/reginfo+xml;text/vnd.in3d.3dml;text/vnd.in3d.spot;mode/iges;application/vnd.intergeo;application/vnd.cinderella;application/vnd.intercon.formnet;application/vnd.isac.fcs;application/ipfix;application/pkix-cert;application/pkixcmp;application/pkix-crl;application/pkix-pkipath;applicaion/vnd.insors.igm;application/vnd.ipunplugged.rcprofile;application/vnd.irepository.package+xml;text/vnd.sun.j2me.app-descriptor;application/java-archive;application/java-vm;application/x-java-jnlp-file;application/java-serializd-object;text/x-java-source,java;application/javascript;application/json;application/vnd.joost.joda-archive;video/jpm;image/jpeg;video/jpeg;application/vnd.kahootz;application/vnd.chipnuts.karaoke-mmd;application/vnd.kde.karbon;aplication/vnd.kde.kchart;application/vnd.kde.kformula;application/vnd.kde.kivio;application/vnd.kde.kontour;application/vnd.kde.kpresenter;application/vnd.kde.kspread;application/vnd.kde.kword;application/vnd.kenameaapp;applicatin/vnd.kidspiration;application/vnd.kinar;application/vnd.kodak-descriptor;application/vnd.las.las+xml;application/x-latex;application/vnd.llamagraphics.life-balance.desktop;application/vnd.llamagraphics.life-balance.exchange+xml;application/vnd.jam;application/vnd.lotus-1-2-3;application/vnd.lotus-approach;application/vnd.lotus-freelance;application/vnd.lotus-notes;application/vnd.lotus-organizer;application/vnd.lotus-screencam;application/vnd.lotus-wordro;audio/vnd.lucent.voice;audio/x-mpegurl;video/x-m4v;application/mac-binhex40;application/vnd.macports.portpkg;application/vnd.osgeo.mapguide.package;application/marc;application/marcxml+xml;application/mxf;application/vnd.wolfrm.player;application/mathematica;application/mathml+xml;application/mbox;application/vnd.medcalcdata;application/mediaservercontrol+xml;application/vnd.mediastation.cdkey;application/vnd.mfer;application/vnd.mfmp;model/mesh;appliation/mads+xml;application/mets+xml;application/mods+xml;application/metalink4+xml;application/vnd.ms-powerpoint.template.macroenabled.12;application/vnd.ms-word.document.macroenabled.12;application/vnd.ms-word.template.macroenabed.12;application/vnd.mcd;application/vnd.micrografx.flo;application/vnd.micrografx.igx;application/vnd.eszigno3+xml;application/x-msaccess;video/x-ms-asf;application/x-msdownload;application/vnd.ms-artgalry;application/vnd.ms-ca-compressed;application/vnd.ms-ims;application/x-ms-application;application/x-msclip;image/vnd.ms-modi;application/vnd.ms-fontobject;application/vnd.ms-excel;application/vnd.ms-excel.addin.macroenabled.12;application/vnd.ms-excelsheet.binary.macroenabled.12;application/vnd.ms-excel.template.macroenabled.12;application/vnd.ms-excel.sheet.macroenabled.12;application/vnd.ms-htmlhelp;application/x-mscardfile;application/vnd.ms-lrm;application/x-msmediaview;aplication/x-msmoney;application/vnd.openxmlformats-officedocument.presentationml.presentation;application/vnd.openxmlformats-officedocument.presentationml.slide;application/vnd.openxmlformats-officedocument.presentationml.slideshw;application/vnd.openxmlformats-officedocument.presentationml.template;application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;application/vnd.openxmlformats-officedocument.spreadsheetml.template;application/vnd.openxmformats-officedocument.wordprocessingml.document;application/vnd.openxmlformats-officedocument.wordprocessingml.template;application/x-msbinder;application/vnd.ms-officetheme;application/onenote;audio/vnd.ms-playready.media.pya;vdeo/vnd.ms-playready.media.pyv;application/vnd.ms-powerpoint;application/vnd.ms-powerpoint.addin.macroenabled.12;application/vnd.ms-powerpoint.slide.macroenabled.12;application/vnd.ms-powerpoint.presentation.macroenabled.12;appliation/vnd.ms-powerpoint.slideshow.macroenabled.12;application/vnd.ms-project;application/x-mspublisher;application/x-msschedule;application/x-silverlight-app;application/vnd.ms-pki.stl;application/vnd.ms-pki.seccat;application/vn.visio;video/x-ms-wm;audio/x-ms-wma;audio/x-ms-wax;video/x-ms-wmx;application/x-ms-wmd;application/vnd.ms-wpl;application/x-ms-wmz;video/x-ms-wmv;video/x-ms-wvx;application/x-msmetafile;application/x-msterminal;application/msword;application/x-mswrite;application/vnd.ms-works;application/x-ms-xbap;application/vnd.ms-xpsdocument;audio/midi;application/vnd.ibm.minipay;application/vnd.ibm.modcap;application/vnd.jcp.javame.midlet-rms;application/vnd.tmobile-ivetv;application/x-mobipocket-ebook;application/vnd.mobius.mbk;application/vnd.mobius.dis;application/vnd.mobius.plc;application/vnd.mobius.mqy;application/vnd.mobius.msl;application/vnd.mobius.txf;application/vnd.mobius.daf;tex/vnd.fly;application/vnd.mophun.certificate;application/vnd.mophun.application;video/mj2;audio/mpeg;video/vnd.mpegurl;video/mpeg;application/mp21;audio/mp4;video/mp4;application/mp4;application/vnd.apple.mpegurl;application/vnd.msician;application/vnd.muvee.style;application/xv+xml;application/vnd.nokia.n-gage.data;application/vnd.nokia.n-gage.symbian.install;application/x-dtbncx+xml;application/x-netcdf;application/vnd.neurolanguage.nlu;application/vnd.na;application/vnd.noblenet-directory;application/vnd.noblenet-sealer;application/vnd.noblenet-web;application/vnd.nokia.radio-preset;application/vnd.nokia.radio-presets;text/n3;application/vnd.novadigm.edm;application/vnd.novadim.edx;application/vnd.novadigm.ext;application/vnd.flographit;audio/vnd.nuera.ecelp4800;audio/vnd.nuera.ecelp7470;audio/vnd.nuera.ecelp9600;application/oda;application/ogg;audio/ogg;video/ogg;application/vnd.oma.dd2+xml;applicatin/vnd.oasis.opendocument.text-web;application/oebps-package+xml;application/vnd.intu.qbo;application/vnd.openofficeorg.extension;application/vnd.yamaha.openscoreformat;audio/webm;video/webm;application/vnd.oasis.opendocument.char;application/vnd.oasis.opendocument.chart-template;application/vnd.oasis.opendocument.database;application/vnd.oasis.opendocument.formula;application/vnd.oasis.opendocument.formula-template;application/vnd.oasis.opendocument.grapics;application/vnd.oasis.opendocument.graphics-template;application/vnd.oasis.opendocument.image;application/vnd.oasis.opendocument.image-template;application/vnd.oasis.opendocument.presentation;application/vnd.oasis.opendocumen.presentation-template;application/vnd.oasis.opendocument.spreadsheet;application/vnd.oasis.opendocument.spreadsheet-template;application/vnd.oasis.opendocument.text;application/vnd.oasis.opendocument.text-master;application/vnd.asis.opendocument.text-template;image/ktx;application/vnd.sun.xml.calc;application/vnd.sun.xml.calc.template;application/vnd.sun.xml.draw;application/vnd.sun.xml.draw.template;application/vnd.sun.xml.impress;application/vnd.sun.xl.impress.template;application/vnd.sun.xml.math;application/vnd.sun.xml.writer;application/vnd.sun.xml.writer.global;application/vnd.sun.xml.writer.template;application/x-font-otf;application/vnd.yamaha.openscoreformat.osfpvg+xml;application/vnd.osgi.dp;application/vnd.palm;text/x-pascal;application/vnd.pawaafile;application/vnd.hp-pclxl;application/vnd.picsel;image/x-pcx;image/vnd.adobe.photoshop;application/pics-rules;image/x-pict;application/x-chat;aplication/pkcs10;application/x-pkcs12;application/pkcs7-mime;application/pkcs7-signature;application/x-pkcs7-certreqresp;application/x-pkcs7-certificates;application/pkcs8;application/vnd.pocketlearn;image/x-portable-anymap;image/-portable-bitmap;application/x-font-pcf;application/font-tdpfr;application/x-chess-pgn;image/x-portable-graymap;image/png;image/x-portable-pixmap;application/pskc+xml;application/vnd.ctc-posml;application/postscript;application/xfont-type1;application/vnd.powerbuilder6;application/pgp-encrypted;application/pgp-signature;application/vnd.previewsystems.box;application/vnd.pvi.ptid1;application/pls+xml;application/vnd.pg.format;application/vnd.pg.osasli;tex/prs.lines.tag;application/x-font-linux-psf;application/vnd.publishare-delta-tree;application/vnd.pmi.widget;application/vnd.quark.quarkxpress;application/vnd.epson.esf;application/vnd.epson.msf;application/vnd.epson.ssf;applicaton/vnd.epson.quickanime;application/vnd.intu.qfx;video/quicktime;application/x-rar-compressed;audio/x-pn-realaudio;audio/x-pn-realaudio-plugin;application/rsd+xml;application/vnd.rn-realmedia;application/vnd.realvnc.bed;applicatin/vnd.recordare.musicxml;application/vnd.recordare.musicxml+xml;application/relax-ng-compact-syntax;application/vnd.data-vision.rdz;application/rdf+xml;application/vnd.cloanto.rp9;application/vnd.jisp;application/rtf;text/richtex;application/vnd.route66.link66+xml;application/rss+xml;application/shf+xml;application/vnd.sailingtracker.track;image/svg+xml;application/vnd.sus-calendar;application/sru+xml;application/set-payment-initiation;application/set-reistration-initiation;application/vnd.sema;application/vnd.semd;application/vnd.semf;application/vnd.seemail;application/x-font-snf;application/scvp-vp-request;application/scvp-vp-response;application/scvp-cv-request;application/svp-cv-response;application/sdp;text/x-setext;video/x-sgi-movie;application/vnd.shana.informed.formdata;application/vnd.shana.informed.formtemplate;application/vnd.shana.informed.interchange;application/vnd.shana.informed.package;application/thraud+xml;application/x-shar;image/x-rgb;application/vnd.epson.salt;application/vnd.accpac.simply.aso;application/vnd.accpac.simply.imp;application/vnd.simtech-mindmapper;application/vnd.commonspace;application/vnd.ymaha.smaf-audio;application/vnd.smaf;application/vnd.yamaha.smaf-phrase;application/vnd.smart.teacher;application/vnd.svd;application/sparql-query;application/sparql-results+xml;application/srgs;application/srgs+xml;application/sml+xml;application/vnd.koan;text/sgml;application/vnd.stardivision.calc;application/vnd.stardivision.draw;application/vnd.stardivision.impress;application/vnd.stardivision.math;application/vnd.stardivision.writer;application/vnd.tardivision.writer-global;application/vnd.stepmania.stepchart;application/x-stuffit;application/x-stuffitx;application/vnd.solent.sdkm+xml;application/vnd.olpc-sugar;audio/basic;application/vnd.wqd;application/vnd.symbian.install;application/smil+xml;application/vnd.syncml+xml;application/vnd.syncml.dm+wbxml;application/vnd.syncml.dm+xml;application/x-sv4cpio;application/x-sv4crc;application/sbml+xml;text/tab-separated-values;image/tiff;application/vnd.to.intent-module-archive;application/x-tar;application/x-tcl;application/x-tex;application/x-tex-tfm;application/tei+xml;text/plain;application/vnd.spotfire.dxp;application/vnd.spotfire.sfs;application/timestamped-data;applicationvnd.trid.tpt;application/vnd.triscape.mxs;text/troff;application/vnd.trueapp;application/x-font-ttf;text/turtle;application/vnd.umajin;application/vnd.uoml+xml;application/vnd.unity;application/vnd.ufdl;text/uri-list;application/nd.uiq.theme;application/x-ustar;text/x-uuencode;text/x-vcalendar;text/x-vcard;application/x-cdlink;application/vnd.vsf;model/vrml;application/vnd.vcx;model/vnd.mts;model/vnd.vtu;application/vnd.visionary;video/vnd.vivo;applicatin/ccxml+xml,;application/voicexml+xml;application/x-wais-source;application/vnd.wap.wbxml;image/vnd.wap.wbmp;audio/x-wav;application/davmount+xml;application/x-font-woff;application/wspolicy+xml;image/webp;application/vnd.webturb;application/widget;application/winhlp;text/vnd.wap.wml;text/vnd.wap.wmlscript;application/vnd.wap.wmlscriptc;application/vnd.wordperfect;application/vnd.wt.stf;application/wsdl+xml;image/x-xbitmap;image/x-xpixmap;image/x-xwindowump;application/x-x509-ca-cert;application/x-xfig;application/xhtml+xml;application/xml;application/xcap-diff+xml;application/xenc+xml;application/patch-ops-error+xml;application/resource-lists+xml;application/rls-services+xml;aplication/resource-lists-diff+xml;application/xslt+xml;application/xop+xml;application/x-xpinstall;application/xspf+xml;application/vnd.mozilla.xul+xml;chemical/x-xyz;text/yaml;application/yang;application/yin+xml;application/vnd.ul;application/zip;application/vnd.handheld-entertainment+xml;application/vnd.zzazz.deck+xml");
        fxProfile.setPreference("browser.helperApps.alwaysAsk.force", false);
        fxProfile.setPreference("browser.download.manager.alertOnEXEOpen", false);
        fxProfile.setPreference("browser.download.manager.focusWhenStarting", false);
        fxProfile.setPreference("browser.download.manager.useWindow", false);
        fxProfile.setPreference("browser.download.manager.showAlertOnComplete", false);
        fxProfile.setPreference("browser.download.manager.closeWhenDone", false);
        FirefoxOptions firefoxOptions = new FirefoxOptions().setProfile(fxProfile);
        return new FirefoxDriver(firefoxOptions);
    }

    private void test() {
        // String html = FUtils.readFile(new File("Z:\\source.html"));
        // String date = mp3PoolScraper.scrapeDate(html);

        //TODO: First browser logs in and search for new release
        // scrapeDriver = getFirefoxDriver("");
        try {
            //     login(scrapeDriver);
            //     String html = scrapeDriver.getPageSource();
            //     String dateOnFirstPage = mp3PoolScraper.scrapeDate(html);
            //     //TODO: If release found -> scrape all links and date
            //     // boolean newReleaseOnMp3Pool = mongoControl.mp3PoolDownloaded
            //     //         .find(eq("releaseDate", dateOnFirstPage)).first() == null;
            //     // if (newReleaseOnMp3Pool) {
            //     // }
            //
            //     // TODO: Find next date
            //     String dateToDownload = getDownloadDate(html, dateOnFirstPage);
            //     System.out.println(dateToDownload);
            //     //TODO Scrape all links
            //     List<String> scrapedLinks = scrapeLinks(dateOnFirstPage, dateToDownload);
            //     scrapedLinks.forEach(System.out::println);
            //     scrapeDriver.quit();
            //TODO: Second browser with download configurations logs in and download links
            // watch download folder to know when everything is downloaded

            //TEST
            String[] links = getStrings();
            List<String> scrapedLinks = Arrays.asList(links);

            String dateToDownload = "07/19/2019";
            String releaseFolderPath = getReleaseFolderPath(dateToDownload);
            WebDriver downloadDriver = getFirefoxDriver(releaseFolderPath);
            login(downloadDriver);
            mp3Logger.log("Downloading release from date: " + dateToDownload);
            downloadDriver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            for (String scrapedLink : scrapedLinks) {
                System.out.println("Downloading: " + scrapedLink);
                downloadDriver.navigate().to(scrapedLink);
            }
            // CustomExecutor downloadMaster = new CustomExecutor(scrapedLinks.size());
            // scrapedLinks.stream()
            //         .map(scrapedLink -> new Thread(() -> {
            //             System.out.println("Downloading: " + scrapedLink);
            //             downloadDriver.navigate().to(scrapedLink);
            //         }))
            //         .forEach(downloadMaster::submit);
            // downloadMaster.WaitUntilTheEnd();
            // TODO: 21.07.2019 Check folder for downloaded files and terminate driver

            //TODO: Schedule release and add to DB
            mp3Logger.log("Release Downloaded: " + dateToDownload);
            // addToScheduleDB(new File(releaseFolderPath));
            // mp3Logger.log("Release Scheduled: " + dateToDownload);
            // mongoControl.mp3PoolDownloaded.insertOne(
            //         new Document("releaseDate", dateOnFirstPage));

        } catch (
                Exception e) {
            e.printStackTrace();
        } finally {
            scrapeDriver.quit();
        }

    }

    @NotNull
    private String[] getStrings() {
        return new String[]{
                "https://mp3poolonline.com/music/download/824748",
                "https://mp3poolonline.com/music/download/824748/66540932",
                "https://mp3poolonline.com/music/download/824746/66540928",
                "https://mp3poolonline.com/music/download/824746/66540927",
                "https://mp3poolonline.com/music/download/824747/66540930",
                "https://mp3poolonline.com/music/download/824747/66540929",
                "https://mp3poolonline.com/music/download/824745/66540926",
                "https://mp3poolonline.com/music/download/824745/66540925",
                "https://mp3poolonline.com/music/download/824744",
                "https://mp3poolonline.com/music/download/824744/66540924",
                "https://mp3poolonline.com/music/download/824742/66540920",
                "https://mp3poolonline.com/music/download/824742/66540919",
                "https://mp3poolonline.com/music/download/824743/66540923",
                "https://mp3poolonline.com/music/download/824743/66540921",
                "https://mp3poolonline.com/music/download/824741/66540918",
                "https://mp3poolonline.com/music/download/824741/66540917",
                "https://mp3poolonline.com/music/download/824740",
                "https://mp3poolonline.com/music/download/824740/66540914",
                "https://mp3poolonline.com/music/download/824738/66540916",
                "https://mp3poolonline.com/music/download/824738/66540915",
                "https://mp3poolonline.com/music/download/824739/66540912",
                "https://mp3poolonline.com/music/download/824739/66540911",
                "https://mp3poolonline.com/music/download/824737/66540908",
                "https://mp3poolonline.com/music/download/824737/66540907",
                "https://mp3poolonline.com/music/download/824736",
                "https://mp3poolonline.com/music/download/824736/66540906",
                "https://mp3poolonline.com/music/download/824735/66540904",
                "https://mp3poolonline.com/music/download/824735/66540903",
                "https://mp3poolonline.com/music/download/824734/66540902",
                "https://mp3poolonline.com/music/download/824734/66540901",
                "https://mp3poolonline.com/music/download/824733",
                "https://mp3poolonline.com/music/download/824733/66540900",
                "https://mp3poolonline.com/music/download/824732/66540898",
                "https://mp3poolonline.com/music/download/824732/66540897",
                "https://mp3poolonline.com/music/download/824731/66540896",
                "https://mp3poolonline.com/music/download/824731/66540895",
                "https://mp3poolonline.com/music/download/824730",
                "https://mp3poolonline.com/music/download/824730/66540892",
                "https://mp3poolonline.com/music/download/824729/66540893",
                "https://mp3poolonline.com/music/download/824729/66540894",
                "https://mp3poolonline.com/music/download/824728/66540890",
                "https://mp3poolonline.com/music/download/824728/66540889",
                "https://mp3poolonline.com/music/download/824727",
                "https://mp3poolonline.com/music/download/824727/66540886",
                "https://mp3poolonline.com/music/download/824726/66540888",
                "https://mp3poolonline.com/music/download/824726/66540887",
                "https://mp3poolonline.com/music/download/824725/66540882",
                "https://mp3poolonline.com/music/download/824725/66540880",
                "https://mp3poolonline.com/music/download/824724/66540881",
                "https://mp3poolonline.com/music/download/824724/66540879",
                "https://mp3poolonline.com/music/download/824723/66540878",
                "https://mp3poolonline.com/music/download/824723/66540877",
                "https://mp3poolonline.com/music/download/824722",
                "https://mp3poolonline.com/music/download/824722/66540875",
                "https://mp3poolonline.com/music/download/824722/66540874",
                "https://mp3poolonline.com/music/download/824721/66540873",
                "https://mp3poolonline.com/music/download/824721/66540872",
                "https://mp3poolonline.com/music/download/824718/66540871",
                "https://mp3poolonline.com/music/download/824718/66540870",
                "https://mp3poolonline.com/music/download/824720/66540869",
                "https://mp3poolonline.com/music/download/824720/66540868",
                "https://mp3poolonline.com/music/download/824719/66540867",
                "https://mp3poolonline.com/music/download/824719/66540866",
                "https://mp3poolonline.com/music/download/824717",
                "https://mp3poolonline.com/music/download/824717/66540865",
                "https://mp3poolonline.com/music/download/824716/66540863",
                "https://mp3poolonline.com/music/download/824716/66540862",
                "https://mp3poolonline.com/music/download/824715/66540861",
                "https://mp3poolonline.com/music/download/824715/66540860",
                "https://mp3poolonline.com/music/download/824748",
                "https://mp3poolonline.com/music/download/824748/66540932",
                "https://mp3poolonline.com/music/download/824747",
                "https://mp3poolonline.com/music/download/824747/66540930",
                "https://mp3poolonline.com/music/download/824746",
                "https://mp3poolonline.com/music/download/824746/66540928",
                "https://mp3poolonline.com/music/download/824745",
                "https://mp3poolonline.com/music/download/824745/66540926",
                "https://mp3poolonline.com/music/download/824744",
                "https://mp3poolonline.com/music/download/824744/66540924",
                "https://mp3poolonline.com/music/download/824743",
                "https://mp3poolonline.com/music/download/824743/66540923",
                "https://mp3poolonline.com/music/download/824742",
                "https://mp3poolonline.com/music/download/824742/66540920",
                "https://mp3poolonline.com/music/download/824741",
                "https://mp3poolonline.com/music/download/824741/66540918",
                "https://mp3poolonline.com/music/download/824740",
                "https://mp3poolonline.com/music/download/824740/66540914",
                "https://mp3poolonline.com/music/download/824739",
                "https://mp3poolonline.com/music/download/824739/66540912",
                "https://mp3poolonline.com/music/download/824738",
                "https://mp3poolonline.com/music/download/824738/66540916",
                "https://mp3poolonline.com/music/download/824737",
                "https://mp3poolonline.com/music/download/824737/66540908",
                "https://mp3poolonline.com/music/download/824736",
                "https://mp3poolonline.com/music/download/824736/66540906",
                "https://mp3poolonline.com/music/download/824735",
                "https://mp3poolonline.com/music/download/824735/66540904",
                "https://mp3poolonline.com/music/download/824734",
                "https://mp3poolonline.com/music/download/824734/66540902",
                "https://mp3poolonline.com/music/download/824733",
                "https://mp3poolonline.com/music/download/824733/66540900",
                "https://mp3poolonline.com/music/download/824732",
                "https://mp3poolonline.com/music/download/824732/66540898",
                "https://mp3poolonline.com/music/download/824731",
                "https://mp3poolonline.com/music/download/824731/66540896",
                "https://mp3poolonline.com/music/download/824730",
                "https://mp3poolonline.com/music/download/824730/66540892",
                "https://mp3poolonline.com/music/download/824729",
                "https://mp3poolonline.com/music/download/824729/66540893",
                "https://mp3poolonline.com/music/download/824728",
                "https://mp3poolonline.com/music/download/824728/66540890",
                "https://mp3poolonline.com/music/download/824727",
                "https://mp3poolonline.com/music/download/824727/66540886",
                "https://mp3poolonline.com/music/download/824726",
                "https://mp3poolonline.com/music/download/824726/66540888",
                "https://mp3poolonline.com/music/download/824725",
                "https://mp3poolonline.com/music/download/824725/66540882",
                "https://mp3poolonline.com/music/download/824724",
                "https://mp3poolonline.com/music/download/824724/66540881",
                "https://mp3poolonline.com/music/download/824723",
                "https://mp3poolonline.com/music/download/824723/66540878",
                "https://mp3poolonline.com/music/download/824722",
                "https://mp3poolonline.com/music/download/824722/66540875",
                "https://mp3poolonline.com/music/download/824722/66540874",
                "https://mp3poolonline.com/music/download/824721",
                "https://mp3poolonline.com/music/download/824721/66540873",
                "https://mp3poolonline.com/music/download/824720",
                "https://mp3poolonline.com/music/download/824720/66540869",
                "https://mp3poolonline.com/music/download/824719",
                "https://mp3poolonline.com/music/download/824719/66540867",
                "https://mp3poolonline.com/music/download/824718",
                "https://mp3poolonline.com/music/download/824718/66540871",
                "https://mp3poolonline.com/music/download/824717",
                "https://mp3poolonline.com/music/download/824717/66540865",
                "https://mp3poolonline.com/music/download/824716",
                "https://mp3poolonline.com/music/download/824716/66540863",
                "https://mp3poolonline.com/music/download/824715",
                "https://mp3poolonline.com/music/download/824715/66540861",
                "https://mp3poolonline.com/music/download/824714",
                "https://mp3poolonline.com/music/download/824714/66540853",
                "https://mp3poolonline.com/music/download/824714/66540854",
                "https://mp3poolonline.com/music/download/824713",
                "https://mp3poolonline.com/music/download/824713/66540859",
                "https://mp3poolonline.com/music/download/824712",
                "https://mp3poolonline.com/music/download/824712/66540855",
                "https://mp3poolonline.com/music/download/824711",
                "https://mp3poolonline.com/music/download/824711/66540851",
                "https://mp3poolonline.com/music/download/824710",
                "https://mp3poolonline.com/music/download/824710/66540850",
                "https://mp3poolonline.com/music/download/824709",
                "https://mp3poolonline.com/music/download/824709/66540848",
                "https://mp3poolonline.com/music/download/824708",
                "https://mp3poolonline.com/music/download/824708/66540846",
                "https://mp3poolonline.com/music/download/824707",
                "https://mp3poolonline.com/music/download/824707/66540844",
                "https://mp3poolonline.com/music/download/824706",
                "https://mp3poolonline.com/music/download/824706/66540842",
                "https://mp3poolonline.com/music/download/824705",
                "https://mp3poolonline.com/music/download/824705/66540840",
                "https://mp3poolonline.com/music/download/824704",
                "https://mp3poolonline.com/music/download/824704/66540838",
                "https://mp3poolonline.com/music/download/824703",
                "https://mp3poolonline.com/music/download/824703/66540834",
                "https://mp3poolonline.com/music/download/824702",
                "https://mp3poolonline.com/music/download/824701",
                "https://mp3poolonline.com/music/download/824701/66540833",
                "https://mp3poolonline.com/music/download/824701/66540836",
                "https://mp3poolonline.com/music/download/824700",
                "https://mp3poolonline.com/music/download/824699",
                "https://mp3poolonline.com/music/download/824699/66540829",
                "https://mp3poolonline.com/music/download/824698",
                "https://mp3poolonline.com/music/download/824697",
                "https://mp3poolonline.com/music/download/824697/66540824",
                "https://mp3poolonline.com/music/download/824697/66540826",
                "https://mp3poolonline.com/music/download/824696",
                "https://mp3poolonline.com/music/download/824695",
                "https://mp3poolonline.com/music/download/824695/66540821",
                "https://mp3poolonline.com/music/download/824695/66540822",
                "https://mp3poolonline.com/music/download/824694",
                "https://mp3poolonline.com/music/download/824693",
                "https://mp3poolonline.com/music/download/824692",
                "https://mp3poolonline.com/music/download/824691",
                "https://mp3poolonline.com/music/download/824691/66540814",
                "https://mp3poolonline.com/music/download/824691/66540815",
                "https://mp3poolonline.com/music/download/824690",
                "https://mp3poolonline.com/music/download/824689",
                "https://mp3poolonline.com/music/download/824689/66540812",
                "https://mp3poolonline.com/music/download/824688",
                "https://mp3poolonline.com/music/download/824687",
                "https://mp3poolonline.com/music/download/824686",
                "https://mp3poolonline.com/music/download/824685",
                "https://mp3poolonline.com/music/download/824684",
                "https://mp3poolonline.com/music/download/824683",
                "https://mp3poolonline.com/music/download/824682",
                "https://mp3poolonline.com/music/download/824681",
                "https://mp3poolonline.com/music/download/824680",
                "https://mp3poolonline.com/music/download/824680/66540801",
                "https://mp3poolonline.com/music/download/824680/66540802",
                "https://mp3poolonline.com/music/download/824679",
                "https://mp3poolonline.com/music/download/824678",
                "https://mp3poolonline.com/music/download/824677"
        };
    }

    private List<String> scrapeLinks(String dateOnFirstPage, String dateToDownload) {
        List<String> scrapedLinks = new ArrayList<>();
        while (true) {
            String html = scrapeDriver.getPageSource();
            scrapedLinks = mp3PoolScraper.scrapeAllLinksOnPage(html, dateToDownload, scrapedLinks);
            nextPage();
            String dateOnTopOfThePage = mp3PoolScraper.scrapeDate(html);
            boolean noDownloadDateOnThePage = !dateOnTopOfThePage.equals(dateOnFirstPage)
                    && !dateOnTopOfThePage.equals(dateToDownload);
            if (noDownloadDateOnThePage) {
                List<String> duplicates = scrapedLinks.stream()
                        .filter(scrapedLink -> scrapedLink.endsWith("/"))
                        .collect(Collectors.toList());
                scrapedLinks.removeAll(duplicates);
                return scrapedLinks;
            }
        }
    }

    private String getDownloadDate(String html, String dateOnFirstPage) {
        while (true) {
            String downloadDate = mp3PoolScraper.previousDateOnThisPage(html, dateOnFirstPage);
            boolean dateOnThisPage = downloadDate != null;
            if (dateOnThisPage) {
                return downloadDate;
            } else {
                nextPage();
                html = scrapeDriver.getPageSource();
            }
        }
    }

    private void nextPage() {
        String currentUrl = scrapeDriver.getCurrentUrl();
        if (currentUrl.contains("page")) {
            int pageNumber = Integer.parseInt(currentUrl.substring(currentUrl.indexOf("=") + 1));
            scrapeDriver.get("https://mp3poolonline.com/viewadminaudio?page=" + (pageNumber + 1));
        } else {
            scrapeDriver.get("https://mp3poolonline.com/viewadminaudio?page=1");
        }
    }

    private String getReleaseFolderPath(String date) throws ParseException {
        SimpleDateFormat DATE_FORMAT =
                new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.setTime(DATE_FORMAT.parse(date));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String dateToDownload = new SimpleDateFormat("ddMM").format(cal.getTime());
        String releaseFolderPath =
                "Z:\\\\TEMP FOR LATER\\2019\\" + CheckDate.getTodayDate() +
                        "\\RECORDPOOL\\" + ("MyMp3Pool " + dateToDownload) + "\\";
        new File(releaseFolderPath).mkdirs();
        return releaseFolderPath;
    }
}
