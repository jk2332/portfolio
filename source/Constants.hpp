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
#define PINCH_CLOUD_DIST_OFFSET     5

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
#define GRID_NUM_X          9
#define GRID_NUM_Y          3

// Board
/** Width of the game world in Box2d units */
#define DEFAULT_WIDTH   32.0f
/** Height of the game world in Box2d units */
#define DEFAULT_HEIGHT  18.0f
#define GRID_SPACING    2
#define GRID_WIDTH      3
#define GRID_HEIGHT     2.5f
#define DRAW_WIDTH     3
#define DRAW_HEIGHT     2
#define DOWN_LEFT_CORNER_X    3
#define DOWN_LEFT_CORNER_Y    4
#define OFFSET_X         0
#define OFFSET_Y         0

#define SIGN  "sign"

#endif /* Constants_hpp */
