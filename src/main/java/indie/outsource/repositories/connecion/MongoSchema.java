package indie.outsource.repositories.connecion;

import com.mongodb.client.model.ValidationOptions;

import indie.outsource.documents.RentMgd;
import indie.outsource.documents.WorkerMgd;
import indie.outsource.documents.user.UserMgd;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

public class MongoSchema {
    private static final Map<String, ValidationOptions> schemaMap = new HashMap<>() {{
        put(UserMgd.class.getSimpleName(), new ValidationOptions().validator(
                Document.parse("""
                {
                  $jsonSchema:{
                      "bsonType": "object",
                      "required": ["login","password"],
                      "properties":{
                          "login":{
                              "bsonType": "string",
                              "minLength": 3,
                              "maxLength": 20
                          },
                         "password": {
                          "bsonType": "string",
                          "minLength": 3,
                          "maxLength": 20
                         },
                      }
                  }
                }
                """
                )
        ));
        put(RentMgd.class.getSimpleName(), new ValidationOptions().validator(
                Document.parse("""
                {
                  $jsonSchema:{
                      "bsonType": "object",
                      "required": ["startDate","worker","user"],
                      "properties":{
                          "startDate":{
                            "bsonType": "date"
                          },
                          "endDate":{
                            "bsonType": "date"
                          }
                          }
                  }
                }
                """
                )
        ));
        put(WorkerMgd.class.getSimpleName(), new ValidationOptions().validator(
                Document.parse("""
                {
                  $jsonSchema:{
                      "bsonType": "object",
                      "required": ["name"],
                      "properties":{
                      "name":{
                          "bsonType": "string",
                           "minLength": 3,
                           "maxLength": 20
                      },
                      "isRented":{
                         "bsonType": "int",
                         "minimum":0,
                         "maximum":1}
                      }
                  }
                }
                """
                )
        ));
    }};

    public static ValidationOptions getSchema(String key) {
        return schemaMap.get(key);
    }
}
