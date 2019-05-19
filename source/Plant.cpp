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
#include "ConstantsMusic.hpp"

using namespace cugl;

bool Plant::init(int x, int y, int rainProb, int shadeProb, float drawscale) {
    _health = 0;
    _x = x;
    _y = y;
    _rainProb = rainProb;
    _shadeProb = shadeProb;
    _drawscale = drawscale;
    _maxStage = 4;
    _defaultHealth = 500;
    _sickStage = 0;
    
    _shadeNeeded = 100;
    _rainNeeded = 100;
    _health = _defaultHealth;

    _stage = 1;
    _shaded = false;
    _state = noNeed;
    _node = nullptr;
    _progress = 0;
    _active = false;
    _activecount = 0;

    _shadeCounter = 0;
    _rainCounter = 0;
    
    // Plant animations
    _actions = ActionManager::alloc();
    _actions2 = ActionManager::alloc();
    _grow = Animate::alloc(0, 8, 1.5f, 1);
    _sparkle = Animate::alloc(0, 12, 0.75f, 1);


    return true;
}

/**
 * Disposes all resources and assets of this Ragdoll
 *
 * Any assets owned by this object will be immediately released.  Once
 * disposed, a Ragdoll may not be used until it is initialized again.
 */
void Plant::dispose() {
    _health = 0;
    _stage = 1;
    _shaded = false;
    _state = noNeed;
    _progress = 0;
    _active = false;
    _shadeCounter = 0;
    
    // Plant animations
    _actions = ActionManager::alloc();
    _grow = Animate::alloc(0, 8, 1.5f, 1);
    for (std::shared_ptr<Texture> a : _textures){
        a = nullptr;
    }
    
    _node = nullptr;
    _signNode = nullptr;
    _assets = nullptr;
    _actions = nullptr;
    _grow = nullptr;
    _sparkle = nullptr;
}

void Plant::setShade(bool f) {
    _shaded = f;
}

void Plant::setRained(bool f) {
    _rained = f && (_state == needRain);
}

void Plant::updateState(int ticks){
    if (_state == dead) {
        return;
    }
    else {
        if  (_attacked) {
            decHealth();
        }
        if (_health >= 1000 && _state != fullgrown){
            _state = noNeed;
        }
        else if (_state == needShade && _state != fullgrown){
            if (_shaded){
                _actions2->activate("current", _sparkle, _sparkleNode);
                incHealth();
                _shadeCounter += 1;
                if (_health >= 0 && _shadeCounter == _shadeNeeded){
                    setState(noNeed);
                    _shadeCounter = 0;
                }
            } else{
                decHealth();
            }
        }
        else if (_state == needRain && _state != fullgrown){
            if (_rained){
                _actions2->activate("current", _sparkle, _sparkleNode);
                incHealth();
                _rainCounter += 1;
                if (_health >= 0 && _rainCounter == _rainNeeded){
                    _rainCounter = 0;
                    setState(noNeed);
                }
            } else{
                decHealth();
            }
        }
        else if (_state == noNeed && (ticks % 250 == 0)){
            if (_stage < _maxStage){
                int statusChance = rand() %  100 + 1;
                if (statusChance < _rainProb) {
                    _state = needRain;
                    _rainProb /= 2;
                } else if (statusChance < _rainProb + _shadeProb) {
                    _state = needShade;
                    _shadeProb /= 1.5;
                }
            }
        } else if (_state == noNeed) {
            if (!_shaded){
                _health += 2;
            }
            else{
                _health -= 2;
            }
        }
    }
    changeSign();

    _shaded = false;
    _rained = false;

    if (_health >= 1000) {
        // not a typo
        _health = 100000;
        upgradeSprite();
    } else if (_health >= 400){
        setSick(0);
    } else if (_health >= 300) {
        setSick(1);
    } else if (_health >= 200) {
        setSick(2);
    } else if (_health >= 100) {
        setSick(3);
    } else if (_health >= 0) {
        setSick(4);
    } else if (_health < 0) {
        setState(dead);
    }
    changeSign();
}

void Plant::setSick(int sickStage) {
    if (sickStage == _sickStage || _active) { return; }
    _sickStage = sickStage;
    if (_sickStage == 0) {
        _deathNode->setVisible(false);
        _node->setVisible(true);
        _deathNode->setFrame(0);
    } else {
        _deathNode->setVisible(true);
        _node->setVisible(false);
        _deathNode->setFrame(sickStage);
    }
    changeSign();
}

void Plant::changeSign() {
    if (_state == needShade) {
        _signNode->setTexture(_assets->get<Texture>("signShade"));
    } else if (_state == needRain) {
        _signNode->setTexture(_assets->get<Texture>("signRain"));
    } else if (_state == dead) {
        if(!playedSound){
            std::shared_ptr<Sound> source = _assets->get<Sound>(PLANTDEATH_EFFECT);
            AudioChannels::get()->playEffect(PLANTDEATH_EFFECT,source,false,EFFECT_VOLUME);
            playedSound = true;
        }
        _signNode->setTexture(_assets->get<Texture>("signSkull"));

    } else if (_stage >= _maxStage) {
        if(!playedSound){
            std::shared_ptr<Sound> source = _assets->get<Sound>(FULLGROWN_EFFECT);
            AudioChannels::get()->playEffect(FULLGROWN_EFFECT,source,false,EFFECT_VOLUME);
            playedSound = true;
        }
        _signNode->setTexture(_assets->get<Texture>("signHappy"));

    } else {
        _signNode->setTexture(_assets->get<Texture>("signSun"));
    }
}

void Plant::upgradeSprite() {
     if (_active && _activecount < 100) {
         _activecount += 1;
     } else if (_active && _activecount >= 100 && _stage < _maxStage) {
        _health = _defaultHealth;
        _activecount = 0;
        _node->setTexture(_assets->get<Texture>(getPlantType() + std::to_string(_stage)));
        _node->setFrame(0);
        _node->setVisible(true);
        _deathNode->setTexture(_assets->get<Texture>(getPlantType() + "-death" + std::to_string(_stage)));
        _deathNode->setFrame(0);
        _deathNode->setVisible(false);
        _active = false;
    } else if (_stage < _maxStage) {
        _active = true;
        _stage += 1;
        if(_stage == _maxStage){_state = fullgrown;}
        _node->setVisible(true);
        _deathNode->setVisible(false);
        _actions->activate("current", _grow, _node);
    }
}

void Plant::update(float dt) {
    _actions->update(dt);
    _actions2->update(dt);
}

void Plant::setState(int s){
    _state = s;
}

void Plant::setSceneNode(const std::shared_ptr<cugl::Node>& node, std::string name, float ds){
    _drawscale = ds;
    _node = AnimationNode::alloc(_assets->get<Texture>(getPlantType() + std::to_string(_stage)), 1, 9);
    _node->setAnchor(Vec2::ANCHOR_BOTTOM_CENTER);
    _node->setScale(0.15f);
    cugl::Vec2 a = _drawscale*cugl::Vec2((DOWN_LEFT_CORNER_X + GRID_WIDTH*_x + GRID_OFFSET_X*_x + GRID_WIDTH/2),
                              (DOWN_LEFT_CORNER_Y + GRID_HEIGHT*_y + GRID_OFFSET_Y*_y - 3*GRID_HEIGHT/4));
    _node->setPosition(a);

    _deathNode = AnimationNode::alloc(_assets->get<Texture>(getPlantType() + "-death" + std::to_string(_stage)), 1, 5);
    _deathNode->setAnchor(Vec2::ANCHOR_BOTTOM_CENTER);
    _deathNode->setScale(0.15f);
    _deathNode->setPosition(a);
    _deathNode->setVisible(false);

    _sparkleNode = AnimationNode::alloc(_assets->get<Texture>("sparkle-film"), 1, 13);
    _sparkleNode->setAnchor(Vec2::ANCHOR_BOTTOM_CENTER);
    cugl::Vec2 c = _drawscale*cugl::Vec2((DOWN_LEFT_CORNER_X + GRID_WIDTH*_x + GRID_OFFSET_X*_x + GRID_WIDTH/2),
                                         (DOWN_LEFT_CORNER_Y + GRID_HEIGHT*_y + GRID_OFFSET_Y*_y - 5*GRID_HEIGHT/8));
    _sparkleNode->setScale(0.2f);
    _sparkleNode->setPosition(c);
    
    _signNode = PolygonNode::allocWithTexture(_assets->get<Texture>("signSun"));
    _signNode->setScale(0.3f);
    _signNode->setAnchor(Vec2::ANCHOR_BOTTOM_CENTER);
    cugl::Vec2 b = _drawscale*cugl::Vec2(DOWN_LEFT_CORNER_X + GRID_WIDTH*_x + GRID_OFFSET_X*_x + GRID_WIDTH,
                              (DOWN_LEFT_CORNER_Y + GRID_HEIGHT*_y + GRID_OFFSET_Y*_y - GRID_HEIGHT));
    _signNode->setPosition(b);
    //Need z-ordering for children of worldNode!
    int plantZ = Z_PLANT_BACK;
    int signZ = Z_SIGN_BACK;
    if(_y == 1){
        plantZ = Z_PLANT_MIDDLE;
        signZ = Z_SIGN_MIDDLE;
    }
    else if(_y == 0){
        plantZ = Z_PLANT_FRONT;
        signZ = Z_SIGN_FRONT;
    }
    node->addChildWithName(_node, name, plantZ);
    node->addChildWithName(_signNode, name + "sign", signZ);
    node->addChildWithName(_sparkleNode, name + "sparkle", signZ);
    node->addChildWithName(_deathNode, name + "death", plantZ);
}
