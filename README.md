# kartta.paikkatietoikkuna.fi

Oskari server extension for paikkatietoikkuna.fi

Powered by http://oskari.org

Use these in oskari-ext.properties:

    # skip publish template migration since we will provide our own
    flyway.1_39_1.skip=true

    view.default=1
    view.template.publish=3
    
    view.default.User=1
    view.default.Guest=2
    
    # Used by Coordinates action route
    projection.library.class=fi.nls.oskari.NLSFIPointTransformer

    # add paikkis as additional module
    db.additional.modules=myplaces,analysis,userlayer,paikkis
    
    # Uncomment these if you want to use different db for user contents and "base Oskari"
    # Note!! The setup.war assumes the database is in localhost:5432!!
    # You need to manually correct the geoserver config if you have them on different host
    # You can run setup again after manually configuring GeoServer stores
    # First run creates stores and namespace, second run will create the layers (only needed if not using localhost)
    #db.myplaces.url=jdbc:postgresql://localhost:5432/oskaridb_myplaces
    #db.analysis.url=jdbc:postgresql://localhost:5432/oskaridb_analysis
    #db.userlayer.url=jdbc:postgresql://localhost:5432/oskaridb_userlayer
    #db.myplaces.jndi.name=jdbc/MyPlacesPool
    #db.analysis.jndi.name=jdbc/AnalysisPool
    #db.userlayer.jndi.name=jdbc/UserLayerPool

Use an additional context file as {jetty}/contexts/pti-front.xml when used with Jetty-Oskari bundle:
 
    <?xml version="1.0"  encoding="ISO-8859-1"?>
    <!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://jetty.eclipse.org/configure.dtd">
    <Configure class="org.eclipse.jetty.server.handler.ContextHandler">
      <Call class="org.eclipse.jetty.util.log.Log" name="debug"><Arg>Paikkis front</Arg></Call>
      <Set name="contextPath">/static</Set>
      <Set name="resourceBase">C:/Omat/alusta/paikkatietoikkuna.fi-frontend</Set>
      <Set name="handler">
        <New class="org.eclipse.jetty.server.handler.ResourceHandler">
          <Set name="welcomeFiles">
            <Array type="String">
              <Item>README.md</Item>
            </Array>
          </Set>
        </New>
      </Set>
    </Configure>
    
Change resourceBase to the directory with clone of https://github.com/nls-oskari/paikkatietoikkuna.fi-frontend

Use an additional JSON file as {jetty}/resources/articles-by-tag-setup-file.json when you want at userguide works:

	{
	    "articles": [
	        {
	            "tags": "fi_ohje_karttaikkuna",
	            "url": "https://www.maanmittauslaitos.fi/asioi-verkossa/palveluiden-kayttoohjeet/paikkatietoikkuna",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "fi_ohje_tyokalut",
	            "url": "https://www.maanmittauslaitos.fi/asioi-verkossa/palveluiden-kayttoohjeet/paikkatietoikkuna/tyokalut ",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "fi_ohje_haku",
	            "url": "https://www.maanmittauslaitos.fi/asioi-verkossa/palveluiden-kayttoohjeet/paikkatietoikkuna/haku",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "fi_ohje_karttatasot",
	            "url": "https://www.maanmittauslaitos.fi/asioi-verkossa/palveluiden-kayttoohjeet/paikkatietoikkuna/karttatasot ",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "fi_ohje_karttajulkaisu",
	            "url": "https://www.maanmittauslaitos.fi/asioi-verkossa/palveluiden-kayttoohjeet/paikkatietoikkuna/karttajulkaisu ",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "fi_ohje_teemakartat",
	            "url": "https://www.maanmittauslaitos.fi/asioi-verkossa/palveluiden-kayttoohjeet/paikkatietoikkuna/teemakartat",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "fi_ohje_analyysi",
	            "url": "https://www.maanmittauslaitos.fi/asioi-verkossa/palveluiden-kayttoohjeet/paikkatietoikkuna/analyysi ",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "en_guide_welcome",
	            "url": "https://www.maanmittauslaitos.fi/en/e-services/instructions-use-our-services/paikkatietoikkuna",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "en_guide_tools",
	            "url": "https://www.maanmittauslaitos.fi/en/e-services/instructions-use-our-services/paikkatietoikkuna/tools",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "en_guide_search",
	            "url": "https://www.maanmittauslaitos.fi/en/e-services/instructions-use-our-services/paikkatietoikkuna/search",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "en_guide_maplayers",
	            "url": "https://www.maanmittauslaitos.fi/en/e-services/instructions-use-our-services/paikkatietoikkuna/map-layers",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "en_guide_publishing",
	            "url": "https://www.maanmittauslaitos.fi/en/e-services/instructions-use-our-services/paikkatietoikkuna/create-map",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "en_guide_thematic",
	            "url": "https://www.maanmittauslaitos.fi/en/e-services/instructions-use-our-services/paikkatietoikkuna/thematic-maps",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "en_guide_analysis",
	            "url": "https://www.maanmittauslaitos.fi/en/e-services/instructions-use-our-services/paikkatietoikkuna/analyse",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "sv_kartfönstret,snappguide",
	            "url": "https://www.maanmittauslaitos.fi/sv/e-tjanster/bruksanvisningar-av-e-tjanster/paikkatietoikkuna",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "sv_guide_tools",
	            "url": "https://www.maanmittauslaitos.fi/sv/e-tjanster/bruksanvisningar-av-e-tjanster/paikkatietoikkuna/verktyg",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "sv_guide_search",
	            "url": "https://www.maanmittauslaitos.fi/sv/e-tjanster/bruksanvisningar-av-e-tjanster/paikkatietoikkuna/sokning",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "sv_guide_maplayers",
	            "url": "https://www.maanmittauslaitos.fi/sv/e-tjanster/bruksanvisningar-av-e-tjanster/paikkatietoikkuna/kartlager",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "sv_guide_publishing",
	            "url": "https://www.maanmittauslaitos.fi/sv/e-tjanster/bruksanvisningar-av-e-tjanster/paikkatietoikkuna/definiera-karta",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "sv_guide_thematic",
	            "url": "https://www.maanmittauslaitos.fi/sv/e-tjanster/bruksanvisningar-av-e-tjanster/paikkatietoikkuna/temakartor",
	            "element": "section.block--mml-content"
	        },
	        {
	            "tags": "sv_guide_analysis",
	            "url": "https://www.maanmittauslaitos.fi/sv/e-tjanster/bruksanvisningar-av-e-tjanster/paikkatietoikkuna/analys",
	            "element": "section.block--mml-content"
	        }
	    ]
	}
 