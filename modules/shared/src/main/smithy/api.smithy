$version: "2.0"

namespace hellosmithy4s.spec

use smithy4s.api#simpleRestJson
use smithy4s.api#uuidFormat

@simpleRestJson
service HelloService {
  version: "1.0.0",
  operations: [Get, Inc, Dec, Update, Create, Delete, GetAll]
}

@readonly
@http(method: "GET", uri: "/api/get/{key}", code: 200)
operation Get {
  input := {
    @httpLabel
    @required 
    key: Key
  }

  output := {
    @required 
    value: Value
  }

  errors: [KeyNotFound]
}

@readonly
@http(method: "GET", uri: "/api/list", code: 200)
operation GetAll {
  output := {
    @required 
    @httpPayload
    pairs: Pairs
  }

  errors: [KeyNotFound]
}

list Pairs {
  member: Pair
}

structure Pair {
  @required 
  key: Key 

  @required 
  value: Value
}

@idempotent
@http(method: "PUT", uri: "/api/create", code: 204)
operation Create {
  input := {
    @required 
    key: Key
  }

  errors: [KeyAlreadyExists]
}

@idempotent
@http(method: "DELETE", uri: "/api/delete/{key}", code: 204)
operation Delete {
  input := {
    @required 
    @httpLabel
    key: Key
  }

  errors: [KeyNotFound]
}

@http(method: "POST", uri: "/api/inc/{key}", code: 204)
operation Inc {
  input := {
    @httpLabel
    @required 
    key: Key
  }
  errors: [KeyNotFound]
}

@http(method: "POST", uri: "/api/dec/{key}", code: 204)
operation Dec {
  input := {
    @httpLabel
    @required 
    key: Key
  }
}

@http(method: "POST", uri: "/api/update/{key}", code: 204)
operation Update {
  input := {
    @httpLabel
    @required 
    key: Key,
    
    @required
    value: Value
  }
  errors: [KeyNotFound]
}

@error("client")
@httpError(404)
structure KeyNotFound {}

@error("client")
@httpError(400)
structure KeyAlreadyExists {}

string Key
integer Value
