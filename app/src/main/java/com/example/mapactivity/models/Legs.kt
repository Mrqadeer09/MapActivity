package com.example.mapactivity.models

class Legs {
    var distance = Distance()
    var duration = Duration()
    var end_address = ""
    var start_address = ""
    var end_location = Loocation()
    var start_location = Loocation()
    var steps = ArrayList<Steps>()
}