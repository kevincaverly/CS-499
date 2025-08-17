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
        
        # Create indexes for better performance
        self._create_indexes()

    def _create_indexes(self):
        """Create indexes for optimal query performance"""
        try:
            # Compound index for rescue type queries (breed, sex, age)
            self.collection.create_index([
                ("breed", 1),
                ("sex_upon_outcome", 1),
                ("age_upon_outcome_in_weeks", 1)
            ], name="rescue_query_index")
            
            # Text index for breed searches
            self.collection.create_index([("breed", "text")], name="breed_text_index")
            
            # Geospatial index for location-based queries
            self.collection.create_index([
                ("location_lat", 1),
                ("location_long", 1)
            ], name="location_index")
            
            # Index for outcome type queries
            self.collection.create_index([("outcome_type", 1)], name="outcome_index")
            
            # Index for animal type queries
            self.collection.create_index([("animal_type", 1)], name="animal_type_index")
            
            print("Indexes created successfully")
        except Exception as e:
            print(f"Error creating indexes: {e}")

# Create method
    def create(self, data):
        if data is not None:
            self.collection.insert_one(data)  # data should be dictionary 
            return True
        else:
            raise Exception("Nothing to save, because data parameter is empty")

# Read method
    def read(self, query=None):
        try:
            cursor = self.collection.find(query)
            return list(cursor)
        except Exception as e:
            print(f"error querying documents: {e}")
            return[]
            
# Update Method   
    def update(self, searchData, updateData):
        if searchData is not None:
                result = self.database.animals.update_many(searchData, { "$set" : updateData })
        else:
            return "{}"
        
        return result.raw_result

# Delete Method         
    def delete(self, deleteData):
        if deleteData is not None:
                result = self.database.animals.delete_many(deleteData)
        else:
            return "{}"
        return result.raw_result

    # New aggregation methods for advanced analytics
    def get_breed_statistics(self, rescue_type=None):
        """Get breed statistics using aggregation pipeline"""
        pipeline = []
        
        # Add match stage if rescue type is specified
        if rescue_type:
            if rescue_type == 'water':
                pipeline.append({
                    '$match': {
                        '$and': [
                            {'$or': [
                                {'breed': {'$regex': 'Labrador Retriever', '$options': 'i'}},
                                {'breed': {'$regex': 'Chesapeake Bay Retriever', '$options': 'i'}},
                                {'breed': {'$regex': 'Newfoundland', '$options': 'i'}}
                            ]},
                            {'sex_upon_outcome': 'Intact Female'},
                            {'age_upon_outcome_in_weeks': {'$gte': 26, '$lte': 156}}
                        ]
                    }
                })
            elif rescue_type == 'mountain':
                pipeline.append({
                    '$match': {
                        '$and': [
                            {'$or': [
                                {'breed': {'$regex': 'German Shepherd', '$options': 'i'}},
                                {'breed': {'$regex': 'Alaskan Malamute', '$options': 'i'}},
                                {'breed': {'$regex': 'Old English Sheepdog', '$options': 'i'}},
                                {'breed': {'$regex': 'Siberian Husky', '$options': 'i'}},
                                {'breed': {'$regex': 'Rottweiler', '$options': 'i'}}
                            ]},
                            {'sex_upon_outcome': 'Intact Male'},
                            {'age_upon_outcome_in_weeks': {'$gte': 26, '$lte': 156}}
                        ]
                    }
                })
            elif rescue_type == 'disaster':
                pipeline.append({
                    '$match': {
                        '$and': [
                            {'$or': [
                                {'breed': {'$regex': 'Doberman Pinscher', '$options': 'i'}},
                                {'breed': {'$regex': 'German Shepherd', '$options': 'i'}},
                                {'breed': {'$regex': 'Golden Retriever', '$options': 'i'}},
                                {'breed': {'$regex': 'Bloodhound', '$options': 'i'}},
                                {'breed': {'$regex': 'Rottweiler', '$options': 'i'}}
                            ]},
                            {'sex_upon_outcome': 'Intact Male'},
                            {'age_upon_outcome_in_weeks': {'$gte': 20, '$lte': 300}}
                        ]
                    }
                })
        
        # Add group stage to count by breed
        pipeline.extend([
            {
                '$group': {
                    '_id': '$breed',
                    'count': {'$sum': 1},
                    'avg_age': {'$avg': '$age_upon_outcome_in_weeks'},
                    'outcomes': {'$push': '$outcome_type'}
                }
            },
            {
                '$sort': {'count': -1}
            },
            {
                '$limit': 10
            }
        ])
        
        try:
            return list(self.collection.aggregate(pipeline))
        except Exception as e:
            print(f"Error in aggregation: {e}")
            return []

    def get_dashboard_analytics(self):
        """Get comprehensive dashboard analytics using aggregation pipeline"""
        pipeline = [
            {
                '$facet': {
                    'total_animals': [
                        {'$count': 'count'}
                    ],
                    'breed_distribution': [
                        {'$group': {'_id': '$breed', 'count': {'$sum': 1}}},
                        {'$sort': {'count': -1}},
                        {'$limit': 10}
                    ],
                    'outcome_statistics': [
                        {'$group': {'_id': '$outcome_type', 'count': {'$sum': 1}}},
                        {'$sort': {'count': -1}}
                    ],
                    'age_distribution': [
                        {
                            '$bucket': {
                                'groupBy': '$age_upon_outcome_in_weeks',
                                'boundaries': [0, 26, 52, 104, 156, 208, 260, 312],
                                'default': 'Older',
                                'output': {'count': {'$sum': 1}}
                            }
                        }
                    ],
                    'gender_distribution': [
                        {'$group': {'_id': '$sex_upon_outcome', 'count': {'$sum': 1}}}
                    ]
                }
            }
        ]
        
        try:
            return list(self.collection.aggregate(pipeline))
        except Exception as e:
            print(f"Error in dashboard analytics: {e}")
            return []

    def get_rescue_candidates(self, rescue_type):
        """Get rescue candidates using aggregation pipeline for better performance"""
        match_stage = {}
        
        if rescue_type == 'water':
            match_stage = {
                '$and': [
                    {'$or': [
                        {'breed': {'$regex': 'Labrador Retriever', '$options': 'i'}},
                        {'breed': {'$regex': 'Chesapeake Bay Retriever', '$options': 'i'}},
                        {'breed': {'$regex': 'Newfoundland', '$options': 'i'}}
                    ]},
                    {'sex_upon_outcome': 'Intact Female'},
                    {'age_upon_outcome_in_weeks': {'$gte': 26, '$lte': 156}}
                ]
            }
        elif rescue_type == 'mountain':
            match_stage = {
                '$and': [
                    {'$or': [
                        {'breed': {'$regex': 'German Shepherd', '$options': 'i'}},
                        {'breed': {'$regex': 'Alaskan Malamute', '$options': 'i'}},
                        {'breed': {'$regex': 'Old English Sheepdog', '$options': 'i'}},
                        {'breed': {'$regex': 'Siberian Husky', '$options': 'i'}},
                        {'breed': {'$regex': 'Rottweiler', '$options': 'i'}}
                    ]},
                    {'sex_upon_outcome': 'Intact Male'},
                    {'age_upon_outcome_in_weeks': {'$gte': 26, '$lte': 156}}
                ]
            }
        elif rescue_type == 'disaster':
            match_stage = {
                '$and': [
                    {'$or': [
                        {'breed': {'$regex': 'Doberman Pinscher', '$options': 'i'}},
                        {'breed': {'$regex': 'German Shepherd', '$options': 'i'}},
                        {'breed': {'$regex': 'Golden Retriever', '$options': 'i'}},
                        {'breed': {'$regex': 'Bloodhound', '$options': 'i'}},
                        {'breed': {'$regex': 'Rottweiler', '$options': 'i'}}
                    ]},
                    {'sex_upon_outcome': 'Intact Male'},
                    {'age_upon_outcome_in_weeks': {'$gte': 20, '$lte': 300}}
                ]
            }
        
        pipeline = [
            {'$match': match_stage},
            {
                '$project': {
                    '_id': 0,
                    'animal_id': 1,
                    'breed': 1,
                    'age_upon_outcome_in_weeks': 1,
                    'sex_upon_outcome': 1,
                    'outcome_type': 1,
                    'location_lat': 1,
                    'location_long': 1,
                    'name': 1
                }
            }
        ]
        
        try:
            return list(self.collection.aggregate(pipeline))
        except Exception as e:
            print(f"Error getting rescue candidates: {e}")
            return []

    def get_location_analytics(self):
        """Get location-based analytics using geospatial aggregation"""
        pipeline = [
            {
                '$match': {
                    'location_lat': {'$exists': True, '$ne': None},
                    'location_long': {'$exists': True, '$ne': None}
                }
            },
            {
                '$group': {
                    '_id': {
                        'lat': {'$round': ['$location_lat', 2]},
                        'lon': {'$round': ['$location_long', 2]}
                    },
                    'count': {'$sum': 1},
                    'breeds': {'$addToSet': '$breed'},
                    'outcomes': {'$addToSet': '$outcome_type'}
                }
            },
            {
                '$sort': {'count': -1}
            },
            {
                '$limit': 50
            }
        ]
        
        try:
            return list(self.collection.aggregate(pipeline))
        except Exception as e:
            print(f"Error in location analytics: {e}")
            return []
