from pymongo import MongoClient
from bson.objectid import ObjectId

class AnimalShelter(object):
    """ CRUD operations for Animal collection in MongoDB """

    def __init__(self, username, password):
        # Initializing the MongoClient. This helps to 
        # access the MongoDB databases and collections.
        # This is hard-wired to use the aac database, the 
        # animals collection, and the aac user.
        # Definitions of the connection string variables are
        # unique to the individual Apporto environment.
        #
        # You must edit the connection variables below to reflect
        # your own instance of MongoDB!
        #
        # Connection Variables
        #
        USER = 'aacuser'
        PASS = 'Girginzoom1'
        HOST = 'nv-desktop-services.apporto.com'
        PORT = 31083
        DB = 'aac'
        COL = 'animals'
        #
        # Initialize Connection
        #
        self.client = MongoClient('mongodb://%s:%s@nv-desktop-services.apporto.com:31083' % (username, password))
        self.database = self.client['AAC']
        self.collection = self.database['%s' % (COL)]

# Complete this create method to implement the C in CRUD.
    def create(self, data):
        if data is not None:
            self.collection.insert_one(data)  # data should be dictionary 
            return True
        else:
            raise Exception("Nothing to save, because data parameter is empty")

# Create method to implement the R in CRUD.
    def read(self, query=None):
        try:
            cursor = self.collection.find(query)
            return list(cursor)
        except Exception as e:
            print(f"error querying documents: {e}")
            return[]
            
        
    def update(self, searchData, updateData):
        if searchData is not None:
                result = self.database.animals.update_many(searchData, { "$set" : updateData })
        else:
            return "{}"
        
        return result.raw_result
            
    def delete(self, deleteData):
        if deleteData is not None:
                result = self.database.animals.delete_many(deleteData)
        else:
            return "{}"
        return result.raw_result
