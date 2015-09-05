// rustler-plate.scad: an electronics tray for Traxxas Rustler
// Daniel Epperson, 2015

// main surface    
linear_extrude(height = 3, center = true) 
difference() {

    // plate
    polygon(points=[[0,5],      [5,0],
                    [70,0],     [130,15],
                    [140,35],   [150,20],
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
    translate([180, 100])
    circle(3, $fn=50);
}


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


// front support
difference()
{
    // front
    translate([0,0,-10])
    linear_extrude(height = 20, center = true) 
    translate([180, 100])
    circle(6, $fn=50);
    
    // front tunnel
    translate([0,0,-7])
    linear_extrude(height = 20, center = true) 
    translate([180, 100])
    circle(3, $fn=50);
}
