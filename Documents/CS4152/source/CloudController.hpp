//
//  WeatherController.hpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/11/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef WeatherController_hpp
#define WeatherController_hpp

#include <stdio.h>
#include "Cloud.hpp"

class WeatherController {
    std::shared_ptr<Cloud> _clouds[1];
};

#endif /* WeatherController_hpp */
