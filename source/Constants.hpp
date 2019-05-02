//
//  Constants.hpp
//  WeatherDefender
//
//  Created by Zaibo  Wang on 4/17/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef Constants_hpp
#define Constants_hpp

#define BKGD_TEXTURE    "background"
#define EARTH_TEXTURE   "earth"
/** Opacity of the foreground mask */
#define FRGD_OPACITY    64

//Level Geography
/** This is adjusted by screen aspect ratio to get the height */
#define SCENE_WIDTH  1024
#define SCENE_HEIGHT 576

// Physics constants for initialization
/** Density of non-crate objects */
#define BASIC_DENSITY       0.0f
/** Density of the crate objects */
#define CRATE_DENSITY       1.0f
/** Friction of non-crate objects */
#define BASIC_FRICTION      0.1f
/** Friction of the crate objects */
#define CRATE_FRICTION      0.2f
/** Angular damping of the crate objects */
#define CRATE_DAMPING       1.0f
/** Collision restitution for all objects */
#define BASIC_RESTITUTION   0.1f
/** Threshold for generating sound on collision */
#define SOUND_THRESHOLD     3
#define PINCH_OFFSET        32

#define PINCH_CLOUD_DIST_OFFSET     5.5
/** The new lessened gravity for this world */
#define WATER_GRAVITY   0.1f
/** Color to outline the physics nodes */
#define STATIC_COLOR    Color4::YELLOW
/** Opacity of the physics outlines */
#define DYNAMIC_COLOR   Color4::GREEN
#define PRIMARY_FONT        "retro"

// Pests
#define LEFT -2
#define RIGHT 10

// Plants
#define noNeed 0
#define needRain 1
#define needSun 2
#define needShade 3
#define dead 4

//Clouds
#define ORIGINAL_SIZE_X    165
#define ORIGINAL_SIZE_Y    84

// Level Model
#define DYNAMIC_COLOR   Color4::GREEN
#define X_COORD   "x"
#define Y_COORD   "y"
#define ID   "id"
#define CLOUDS_FIELD   "cloud"
#define CLOUD_PRIORITY   4
#define TEXTURE_FIELD   "texture"
#define HEIGHT_FIELD   "height"
#define WIDTH_FIELD   "width"
#define TIME_FIELD   "time"
#define TYPE   "type"
#define GRID_NUM_X          7
#define GRID_NUM_Y          3

// Board
/** Width of the game world in Box2d units */
#define DEFAULT_WIDTH   32.0f
/** Height of the game world in Box2d units */
#define DEFAULT_HEIGHT  18.0f
#define GRID_SPACING    2
#define GRID_WIDTH      3.2
#define GRID_HEIGHT     2.7f
#define DRAW_WIDTH     4
#define DRAW_HEIGHT     3
#define DOWN_LEFT_CORNER_X    3
#define DOWN_LEFT_CORNER_Y    3.5
#define OFFSET_X         0
#define OFFSET_Y         0
#define GRID_OFFSET_X   0.65
#define GRID_OFFSET_Y   0.3

#define SIGN  "sign"
#define UI_ZVALUE 100

//Cloud Particles
#define PARTICLE_NUM 0
#define PARTICLE_FACTOR_W   0.005f
#define PARTICLE_FACTOR_H   0.01f
#define MAX_JOSTLE 8.0f
#define MAX_VELOCITY 10.0f

#define SWIPE_VERT_OFFSET   3.5
#define GES_COOLDOWN      20
#define SPLIT_COOLDOWN      30

#endif /* Constants_hpp */
