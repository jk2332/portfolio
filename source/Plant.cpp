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

bool Plant::init(int x, int y, int rainProb, int shadeProb, std::vector<std::shared_ptr<Texture>> textures, float drawscale) {
    _health = 0;
    _x = x;
    _y = y;
    _rainProb = rainProb;
    _shadeProb = shadeProb;
    _drawscale = drawscale;
    _textures = textures;
    
    _stage = 0;
    _maxStage = textures.size() - 1;
    _shaded = false;
    _state = noNeed;
    _node = nullptr;
    _progress = 0;
    
    _shadeCounter = 0;
    
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

void Plant::setShade(bool f) {
    _shaded = f;
}

void Plant::setRained(bool f) {
    _rained = f && (_state == needRain);
}

void Plant::updateState(){
    //CULog("Update %s state", getName().c_str());
    if (_state == dead || _stage == _maxStage){
        //do nothing
        return;
    }
    else {
        if (_state == needShade){
            CULog("update needs shade");
            if (_shaded){
                incHealth();
                _shadeCounter += 1;
                if (_health >= 0 && _shadeCounter == 2){
                    setState(noNeed);
                    _shadeCounter = 0;
                    _progress += 2;
                }
            } else{
                decHealth();
                if (_progress >= 1) {
                    _progress -= 1;
                }
            }
        }
        else if (_state == needRain){
            if (_rained){
                incHealth();
                if (_health >= 0){
                    setState(noNeed);
                    _progress += 2;
                }
            } else{
                decHealth();
                if (_progress >= 1) {
                    _progress -= 1;
                }
            }
        }
        else if (_state == noNeed){
            int statusChance = rand() %  100 + 1;
            if (statusChance < _rainProb) {
                CULog("%s now needs rain, rng is %d", getName().c_str(), statusChance);
                _state = needRain;
                _rainProb /= 2;
            } else if (statusChance < _rainProb + _shadeProb) {
                CULog("%s now needs shade, rng is %d", getName().c_str(), statusChance);
                _state = needShade;
                _shadeProb /= 1.5;
            } else {
                CULog("%s still needs nothing, rng is %d", getName().c_str(), statusChance);
                if (!_shaded){
                    _progress += 1;
                }
            }
        } else if (_state == needSun) {
            CULog("update needs sun");
            if (!_shaded){
                incHealth();
                if (_health >= 0){setState(noNeed);}
            }
            else{
                decHealth();
                if (_progress >= 1) {
                    _progress -= 1;
                }
            }
        }
        
        _shaded = false;
        _rained = false;
        
        if(_health <= -healthLimit){
            setState(dead);
        } else if (_progress >= 4) {
            upgradeSprite();
        }
    }
}

void Plant::upgradeSprite() {
    _progress = 0;
    if (_stage < _maxStage) {
        _stage += 1;
        _node->setTexture(_textures[_stage]);
    }
}

void Plant::setState(int s){
    _state = s;
    _health = 0;
}

void Plant::setSceneNode(const std::shared_ptr<cugl::Node>& node, std::string name){
    std::shared_ptr<PolygonNode> plant_node = PolygonNode::allocWithTexture(_textures[_stage]);
    cugl::Vec2 a = cugl::Vec2((DOWN_LEFT_CORNER_X + GRID_WIDTH*_x + GRID_WIDTH/2)*32.0f, (DOWN_LEFT_CORNER_Y + GRID_HEIGHT*_y - GRID_HEIGHT/2)*32.0f);
    plant_node->setPosition(a);
    plant_node->setScale(0.15f);
    _node = plant_node;
    node->addChildWithName(plant_node, name);
}
