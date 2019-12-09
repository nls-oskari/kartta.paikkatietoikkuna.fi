UPDATE oskari_statistical_datasource SET config='{
    "url": "https://statdb.luke.fi/pxweb/api/v1/{language}/LUKE",
    "info": {
        "url": "https://stat.luke.fi/"
    },
    "regionKey": "ELY-keskus"
}' where locale LIKE '%Luonnonvarakeskus%';