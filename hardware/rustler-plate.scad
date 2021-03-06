// rustler-plate.scad: an electronics tray for Traxxas Rustler
// Daniel Epperson, 2015

// main surface    
linear_extrude(height = 3, center = true) 
difference() {

    // plate
    polygon(points=[[0,5],      [5,0],
                    [70,0],     [130,15],
                    [135,35],   [150,20],
                    [210,35],   [210,105],
                    [70,140],
                    [5,140],    [0,135],
                    [0,105],    [40,105],
                    [40,35],    [0,35]
    ]);
    
    // ESC hole
    translate([40, 70])
    circle(12, $fn=50);
    
    // right bolt hole
    translate([15, 15])
    circle(3, $fn=50);
    
    // left bolt hole
    translate([15, 125])
    circle(3, $fn=50);
    
    // front bolt hole
    translate([177, 100])
    circle(3, $fn=50);
    
    // ioio holes
    translate([140, 105]) circle(2, $fn=50);
    translate([133, 82])  circle(2, $fn=50);
    translate([75,  120]) circle(2, $fn=50);
    translate([68,  97])  circle(2, $fn=50);    
    
    // phone mounts
    translate([205, 80]) circle(2, $fn=50);    
    translate([205, 60]) circle(2, $fn=50);    
    translate([75,  30]) circle(2, $fn=50);

}

color("blue")
// rear supports
difference()
{
    translate([0,0,-25])
    linear_extrude(height = 50, center = true) 
    {

        // right 
        translate([15, 15])
        circle(6, $fn=50);

        // left 
        translate([15, 125])
        circle(6, $fn=50);
    }
    translate([0,0,-22])
    linear_extrude(height = 50, center = true) 
    {

        // right tunnel
        translate([15, 15])
        circle(3, $fn=50);

        // left tunnel
        translate([15, 125])
        circle(3, $fn=50);
    }
}

// right support reinforcements
color("red")
translate([30,15,1])
rotate([90,180,0])
linear_extrude(height = 5, center = true) 
polygon(points=[[0,0], [10,0], [10,20]]);

color("red")
translate([7,28,1])
rotate([90,180,120])
linear_extrude(height = 5, center = true) 
polygon(points=[[0,0], [10,0], [10,20]]);

color("red")
translate([7,2,1])
rotate([90,180,240])
linear_extrude(height = 5, center = true) 
polygon(points=[[0,0], [10,0], [10,20]]);


// left support reinforcements
color("red")
translate([30,125,1])
rotate([90,180,0])
linear_extrude(height = 5, center = true) 
polygon(points=[[0,0], [10,0], [10,20]]);

color("red")
translate([7,138,1])
rotate([90,180,120])
linear_extrude(height = 5, center = true) 
polygon(points=[[0,0], [10,0], [10,20]]);

color("red")
translate([7,112,1])
rotate([90,180,240])
linear_extrude(height = 5, center = true) 
polygon(points=[[0,0], [10,0], [10,20]]);

color("blue")
// front support
difference()
{
    // front
    translate([0,0,-10])
    linear_extrude(height = 20, center = true) 
    translate([177, 100])
    circle(6, $fn=50);
    
    // front tunnel
    translate([0,0,-7])
    linear_extrude(height = 20, center = true) 
    translate([177, 100])
    circle(3, $fn=50);
}

// front support reinforcements
color("red")
translate([192,100,1])
rotate([90,180,0])
linear_extrude(height = 5, center = true) 
polygon(points=[[0,0], [10,0], [10,20]]);

color("red")
translate([169,113,1])
rotate([90,180,120])
linear_extrude(height = 5, center = true) 
polygon(points=[[0,0], [10,0], [10,20]]);

color("red")
translate([169,87,1])
rotate([90,180,240])
linear_extrude(height = 5, center = true) 
polygon(points=[[0,0], [10,0], [10,20]]);