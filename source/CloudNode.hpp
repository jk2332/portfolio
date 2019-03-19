//
//  CloudNode.hpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/18/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef CloudNode_hpp
#define CloudNode_hpp

#include <stdio.h>
#include <cugl/2d/CUPolygonNode.h>

using namespace cugl;

class CloudNode : public PolygonNode {
  
    void draw(const std::shared_ptr<SpriteBatch>& batch, const Mat4& transform, Color4 tint) {}
    
    
};

#endif /* CloudNode_hpp */
