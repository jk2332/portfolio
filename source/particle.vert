//
//  particle.vert
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/13/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

/**
 * Uniform texture shader for OpenGL and OpenGL ES
 */
const char* oglCTVert = SHADER(
////////// SHADER BEGIN /////////

//layout (location = 0)
in vec2 position;
in vec2 texCoords;
                                         
out vec2 TexCoords;
out vec4 ParticleColor;

uniform mat4 projection;
uniform vec2 offset;
uniform vec4 color;

void main(){
 float scale = 10.0f;
 TexCoords = texCoords;
 ParticleColor = color;
 gl_Position = projection * vec4((position * scale) + offset, 0.0, 1.0);
}

/////////// SHADER END //////////
);
