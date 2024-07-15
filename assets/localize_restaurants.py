import random
import sys
from pymongo import MongoClient


def insert_localized_restaurants(latitude, longitude):
    client = MongoClient()
    db = client["restaurant-database"]
    restaurant_collection = db["restaurants"]
    num_restaurants = restaurant_collection.count_documents({})

    start_index = round(random.random() * (num_restaurants / 2))
    num_restaurants_to_change = 50

    cursor = db["restaurants"].find(skip=start_index, limit=num_restaurants_to_change)

    for restaurant in cursor:
        restaurant["latitude"] = latitude
        restaurant["longitude"] = longitude
        restaurant_collection.find_one_and_replace(
            {"_id": restaurant["_id"]}, restaurant
        )

    print("Restaurants around co-ordinates created.")


if __name__ == "__main__":
    if(len(sys.argv)) < 3:
        print("Both arguments were not present.")
        exit()
    latitude = float(sys.argv[1])
    longitude = float(sys.argv[2])
    print("Latitude: {}, Longitude: {}".format(latitude, longitude))
    insert_localized_restaurants(latitude, longitude)
