//
//  CloudNode.cpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/18/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "CloudNode.hpp"
#include <cugl/2d/CUPolygonNode.h>

using namespace cugl;

void PolygonNode::draw(const std::shared_ptr<SpriteBatch>& batch, const Mat4& transform, Color4 tint) {
    if (!_rendered) {
        generateRenderData();
    }
    
    batch->end();
    //my custom code
    
//    batch->begin(getScene()->getCamera()->getCombined());
    
    batch->setColor(tint);
    batch->setTexture(_texture);
    batch->setBlendEquation(_blendEquation);
    batch->setBlendFunc(_srcFactor, _dstFactor);
    batch->fill(_vertices.data(),(unsigned int)_vertices.size(),0,
                _polygon.getIndices().data(),(unsigned int)_polygon.getIndices().size(),0,
                transform);
    }
