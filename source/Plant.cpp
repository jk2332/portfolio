//
//  Plant.cpp
//  Weather Defender (Mac)
//
//  Created by 김지원 on 2/23/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "Plant.hpp"
#include "Board.hpp"
#include <Box2D/Dynamics/Joints/b2RevoluteJoint.h>
#include <Box2D/Dynamics/Joints/b2WeldJoint.h>
#include <Box2D/Dynamics/b2World.h>


using namespace cugl;

bool Plant::init(int x, int y, std::shared_ptr<Texture> texture, float drawscale) {
    _health = 0;
    _x = x;
    _y = y;
    _drawscale = drawscale;
    _texture = texture;
    isShaded = false;
    _state = rand() % 4;
    if (_state == needRain){
        _state = needShade;
    }
    else if (_state == noNeed){
        _state = needSun;
    }
    return true;
}

/**
 * Disposes all resources and assets of this Ragdoll
 *
 * Any assets owned by this object will be immediately released.  Once
 * disposed, a Ragdoll may not be used until it is initialized again.
 */
void Plant::dispose() {
    //_texture = nullptr;
}

void Plant::updateState(){
    if (_state == dead){
        //do nothing
    }
    else{
        if (_state == needShade){
            if (isShaded){
                incHealth();
                if (_health >= 0){setState(noNeed);}
            }
            else{decHealth();}
        }
        else if (_state == needRain){
            //nothing yet
        }
        else if (_state == needSun){
            if (!isShaded){
                incHealth();
                if (_health >= 0){setState(noNeed);}
            }
            else{decHealth();}
        }
        if(_health <= -healthLimit){
            setState(dead);
        }
    }
    
}

void Plant::setState(int s){
    _state = s;
    _health = 0;
}

void Plant::setSceneNode(const std::shared_ptr<cugl::Node>& node){
    std::shared_ptr<PolygonNode> plant_node = PolygonNode::allocWithTexture(_texture);
    Board b;
    plant_node->setPosition(b.getGridCenterPos(32.0f, Vec2(_x,_y)));
    plant_node->setScale(0.15f);
    node->addChildWithName(plant_node, "plant"+std::to_string(_x) + std::to_string(_y));
}


