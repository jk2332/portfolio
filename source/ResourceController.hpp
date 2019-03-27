//
//  ResourceController.hpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/11/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef ResourceController_hpp
#define ResourceController_hpp

#include <stdio.h>
#include "ResourceCloud.hpp"
#include "Cloud.hpp"

class ResourceController {
    std::shared_ptr<ResourceCloud> _clouds[1];
    
    void addToCloud(Cloud c);
};
#endif /* ResourceController_hpp */
