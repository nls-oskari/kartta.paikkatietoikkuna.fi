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
    # These need to match names in SqlMapConfig_*.xml under webapp-map
    db.analysis.jndi.name=jdbc/AnalysisPool
    db.userlayer.jndi.name=jdbc/UserLayerPool
