# Export data from archive
mongorestore --host localhost --db restaurant-database --gzip --archive=data-dump

# Localise a few random restaurants to be around our set latitude,longitude
cd docker-entrypoint-initdb.d
python3 ./localize_restaurants.py $LOCALIZE_LATITUDE $LOCALIZE_LONGITUDE