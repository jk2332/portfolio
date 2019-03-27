//
//  particle.frag
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/13/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//


#if CU_GL_PLATFORM == CU_GL_OPENGL

/**
 * The fragment shader for OpenGL
 */
const char* oglCTFrag = SHADER(
////////// SHADER BEGIN /////////

//#version 330 core
in vec2 TexCoords;
in vec4 ParticleColor;
out vec4 color;

uniform sampler2D sprite;

void main(){
    color = vec4(1.0,1.0,1.0,1.0);//(texture(sprite, TexCoords) * ParticleColor);
}

/////////// SHADER END //////////
);

#else

/**
 * The fragment shader for OpenGL ES
 */
const char* oglCTFrag = SHADER(
////////// SHADER BEGIN /////////

// This one line is all the difference
precision mediump float;

//#version 330 core
in vec2 TexCoords;
in vec4 ParticleColor;
out vec4 color;

uniform sampler2D sprite;

void main(){
//    color = (texture(sprite, TexCoords) * ParticleColor);
    color = vec4(0.0,0.0,0.0,1.0);
}
/////////// SHADER END //////////
);

#endif
