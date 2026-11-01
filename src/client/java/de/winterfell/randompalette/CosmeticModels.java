package de.winterfell.randompalette;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;


/**
 * Client-only hand-built 3D geometry for the LOCAL player.
 * Renders no entities and sends no packets. Visible in third person only.
 * Independent head and back slots; lightweight cube/feather geometry.
 */
public final class CosmeticModels {
    private static final Identifier WHITE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/block/white_concrete.png");
    private static final int LIGHT = 0x00F000F0;

    private CosmeticModels() {}

    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            Minecraft mc = Minecraft.getInstance();
            PaletteConfig cfg = RandomPaletteClient.config;
            if (mc.player == null || mc.level == null || cfg == null || !cfg.cosmeticsEnabled
                    || mc.player.isSpectator() || mc.player.isInvisible()
                    || mc.options.getCameraType().isFirstPerson()
                    || ("none".equals(cfg.headCosmetic) && "none".equals(cfg.backCosmetic)))
                return;

            String head = cfg.headCosmetic;
            String back = cfg.backCosmetic;
            if (!CosmeticEffects.validHead(head) || !CosmeticEffects.validBack(back)) return;

            var camera = context.levelState().cameraRenderState.pos;
            PoseStack pose = context.poseStack();
            pose.pushPose();
            pose.translate(mc.player.getX() - camera.x,
                    mc.player.getY() - camera.y - (mc.player.isShiftKeyDown() ? 0.21 : 0),
                    mc.player.getZ() - camera.z);

            // Local -Z points behind the character when yaw is zero.
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-mc.player.getYRot()));
            float flutter = (float)Math.sin(System.nanoTime() * 0.0000000023) * 0.065f;
            context.submitNodeCollector().submitCustomGeometry(
                    pose, RenderTypes.entityTranslucent(WHITE),
                    (transformed, buffer) -> {
                        drawHead(transformed, buffer, head);
                        drawBack(transformed, buffer, back, flutter);
                    });
            pose.popPose();
        });
    }

    private static void drawHead(PoseStack.Pose p, VertexConsumer v, String id) {
        switch (id) {
            case "crown", "ice_crown" -> {
                int metal = "crown".equals(id) ? 0xFFFFC63F : 0xFF79D9FF;
                int jewel = "crown".equals(id) ? 0xFFDE354B : 0xFF2B7BC8;
                // Circlet wraps the 3D player head.
                box(p,v,-.32f,1.86f,-.32f,.32f,1.94f,-.26f,metal);
                box(p,v,-.32f,1.86f,.26f,.32f,1.94f,.32f,metal);
                box(p,v,-.32f,1.86f,-.32f,-.26f,1.94f,.32f,metal);
                box(p,v,.26f,1.86f,-.32f,.32f,1.94f,.32f,metal);
                for (int i = -1; i <= 1; i++) {
                    float x = i * .245f;
                    box(p,v,x-.045f,1.94f,-.325f,x+.045f,2.12f,-.255f,metal);
                    box(p,v,x-.037f,1.94f,.255f,x+.037f,2.09f,.325f,metal);
                }
                box(p,v,-.06f,1.89f,-.34f,.06f,1.99f,-.32f,jewel);
            }
            case "horns" -> {
                for (int side : new int[]{-1,1}) {
                    float x = side * .24f;
                    box(p,v,x-.105f,1.90f,-.14f,x+.105f,2.13f,.11f,0xFF2B1830);
                    beam(p,v,x,2.10f,side*.37f,2.34f,-.10f,.08f,0xFF8D2439);
                    beam(p,v,side*.37f,2.34f,side*.42f,2.47f,-.10f,.055f,0xFFE85A64);
                }
            }
            case "halo" -> {
                for (int i=0;i<16;i++) {
                    double a = i * Math.PI * 2 / 16;
                    float x=(float)Math.cos(a)*.34f;
                    float z=(float)Math.sin(a)*.34f;
                    box(p,v,x-.06f,2.18f,z-.06f,x+.06f,2.23f,z+.06f,0xFFF8DF83);
                }
            }
            case "top_hat" -> {
                box(p,v,-.43f,1.91f,-.43f,.43f,1.98f,.43f,0xFF1E2131);
                box(p,v,-.29f,1.97f,-.29f,.29f,2.35f,.29f,0xFF25263A);
                box(p,v,-.296f,2.04f,-.296f,.296f,2.10f,.296f,0xFF8E2339);
                box(p,v,-.29f,2.34f,-.29f,.29f,2.38f,.29f,0xFF2B2B40);
            }
            case "cat_ears" -> {
                for (int side:new int[]{-1,1}) {
                    float x=side*.24f;
                    triangle(p,v,x-.14f,1.92f,x+.14f,1.92f,x,2.21f,-.23f,-.08f,0xFF37313A);
                    triangle(p,v,x-.075f,1.96f,x+.075f,1.96f,x,2.14f,-.241f,-.236f,0xFFDA758C);
                }
            }
            case "headphones" -> {
                box(p,v,-.39f,1.61f,-.12f,-.31f,1.85f,.16f,0xFF182131);
                box(p,v,.31f,1.61f,-.12f,.39f,1.85f,.16f,0xFF182131);
                box(p,v,-.37f,1.89f,-.10f,.37f,1.98f,.09f,0xFF343D51);
                box(p,v,-.395f,1.68f,-.14f,-.38f,1.77f,.14f,0xFF41B5DF);
                box(p,v,.38f,1.68f,-.14f,.395f,1.77f,.14f,0xFF41B5DF);
            }
            default -> {}
        }
    }

    private static void drawBack(PoseStack.Pose p, VertexConsumer v, String id, float flap) {
        switch (id) {
            case "demon_wings" -> wings(p,v,flap,0xFF311C30,0xFFAF283F,0xFFEE6874,false);
            case "dragon_wings" -> wings(p,v,flap,0xFF1D2E27,0xFF36724D,0xFF7BCF77,false);
            case "ice_wings" -> wings(p,v,flap,0xFF236181,0xFF62C6EA,0xFFC4F5FF,true);
            case "angel_wings" -> wings(p,v,flap,0xFFDADDE8,0xFFF0F1F6,0xFFFFF7C4,true);
            case "red_cape", "royal_cape" -> {
                int fabric = "red_cape".equals(id) ? 0xFF8D2237 : 0xFF273B91;
                int border = "red_cape".equals(id) ? 0xFF241A2B : 0xFFF0CC66;
                box(p,v,-.32f,.48f,-.40f,.32f,1.54f,-.31f,border);
                box(p,v,-.275f,.52f,-.42f,.275f,1.49f,-.395f,fabric);
                box(p,v,-.29f,1.46f,-.44f,.29f,1.55f,-.35f,border);
                if ("royal_cape".equals(id)) {
                    box(p,v,-.035f,.60f,-.445f,.035f,1.37f,-.425f,0xFFFFD57A);
                }
            }
            case "backpack" -> {
                box(p,v,-.34f,.90f,-.46f,.34f,1.55f,-.28f,0xFF63462D);
                box(p,v,-.29f,.98f,-.49f,.29f,1.43f,-.452f,0xFF926B42);
                box(p,v,-.06f,1.11f,-.52f,.06f,1.23f,-.48f,0xFFE5C771);
                box(p,v,-.37f,1.23f,-.29f,-.31f,1.56f,-.09f,0xFF312D2B);
                box(p,v,.31f,1.23f,-.29f,.37f,1.56f,-.09f,0xFF312D2B);
            }
            case "jetpack" -> {
                box(p,v,-.31f,.84f,-.42f,.31f,1.51f,-.25f,0xFF2B3947);
                for (int side:new int[]{-1,1}) {
                    float x=side*.20f;
                    box(p,v,x-.13f,.80f,-.58f,x+.13f,1.44f,-.37f,0xFF7D8998);
                    box(p,v,x-.09f,.75f,-.54f,x+.09f,.85f,-.40f,0xFFEA6C38);
                    box(p,v,x-.09f,1.28f,-.61f,x+.09f,1.35f,-.57f,0xFF4DD0FF);
                }
            }
            case "back_sword" -> {
                beam(p,v,-.29f,.73f,.36f,1.61f,-.35f,.13f,0xFFB2CAD8);
                beam(p,v,-.33f,.68f,-.12f,.96f,-.38f,.17f,0xFF31253B);
                beam(p,v,-.49f,1.01f,.05f,.83f,-.35f,.11f,0xFFD4B45E);
                box(p,v,-.39f,.62f,-.40f,-.27f,.72f,-.29f,0xFFDC515B);
            }
            default -> {}
        }
    }

    private static void wings(PoseStack.Pose p, VertexConsumer v, float flap,
                              int outline, int membrane, int highlight, boolean feathers) {
        for (int side:new int[]{-1,1}) {
            float a=side*(.25f+flap*.2f), b=side*(1.04f+flap);
            float c=side*(1.29f+flap);
            // Raised 3D wing bones with an actual extruded membrane.
            beam(p,v,a,1.42f,b,1.88f,-.38f,.115f,outline);
            beam(p,v,b,1.88f,c,1.56f,-.38f,.090f,outline);
            beam(p,v,a,1.39f,side*.73f,.96f,-.40f,.09f,outline);
            triangle(p,v,a,1.43f,b,1.88f,c,1.48f,-.48f,-.39f,membrane);
            triangle(p,v,a,1.43f,c,1.48f,side*.78f,1.03f,-.48f,-.39f,membrane);
            triangle(p,v,a,1.43f,side*.78f,1.03f,side*.39f,.88f,-.48f,-.39f,membrane);
            if (feathers) {
                for (int i=0;i<5;i++) {
                    float x=side*(.46f+i*.15f);
                    beam(p,v,x,1.42f-i*.08f,x+side*.09f,1.08f-i*.08f,-.51f,.06f,highlight);
                }
            } else {
                for (int i=0;i<3;i++) {
                    float x=side*(.50f+i*.24f);
                    beam(p,v,x,1.48f,x+side*.11f,1.17f,-.50f,.058f,highlight);
                }
            }
        }
    }

    /** A solid 3D cuboid composed of six textured, tinted faces. */
    private static void box(PoseStack.Pose p, VertexConsumer v,
                            float x0,float y0,float z0,float x1,float y1,float z1,int color) {
        quad(p,v,x0,y0,z0, x1,y0,z0, x1,y1,z0, x0,y1,z0,color);
        quad(p,v,x1,y0,z1, x0,y0,z1, x0,y1,z1, x1,y1,z1,color);
        quad(p,v,x0,y0,z1, x0,y0,z0, x0,y1,z0, x0,y1,z1,color);
        quad(p,v,x1,y0,z0, x1,y0,z1, x1,y1,z1, x1,y1,z0,color);
        quad(p,v,x0,y1,z0, x1,y1,z0, x1,y1,z1, x0,y1,z1,color);
        quad(p,v,x0,y0,z1, x1,y0,z1, x1,y0,z0, x0,y0,z0,color);
    }

    /** Thick diagonal rod in the XY plane. */
    private static void beam(PoseStack.Pose p, VertexConsumer v,
                             float x0,float y0,float x1,float y1,float z,float width,int color) {
        float dx=x1-x0, dy=y1-y0;
        float length=(float)Math.sqrt(dx*dx+dy*dy);
        if(length<.00001f) return;
        float nx=-dy/length*width*.5f, ny=dx/length*width*.5f;
        float[] xy={x0+nx,y0+ny,x1+nx,y1+ny,x1-nx,y1-ny,x0-nx,y0-ny};
        float halfZ=width*.5f;
        quad(p,v,xy[0],xy[1],z-halfZ,xy[2],xy[3],z-halfZ,
                xy[4],xy[5],z-halfZ,xy[6],xy[7],z-halfZ,color);
        quad(p,v,xy[6],xy[7],z+halfZ,xy[4],xy[5],z+halfZ,
                xy[2],xy[3],z+halfZ,xy[0],xy[1],z+halfZ,color);
        for (int i=0;i<4;i++) {
            int j=(i+1)%4;
            quad(p,v,xy[2*i],xy[2*i+1],z-halfZ,xy[2*j],xy[2*j+1],z-halfZ,
                    xy[2*j],xy[2*j+1],z+halfZ,xy[2*i],xy[2*i+1],z+halfZ,color);
        }
    }

    /** Double-sided triangular prism for wing membranes and ears. */
    private static void triangle(PoseStack.Pose p, VertexConsumer v,
                                 float ax,float ay,float bx,float by,float cx,float cy,
                                 float z0,float z1,int color) {
        quad(p,v,ax,ay,z0,bx,by,z0,cx,cy,z0,cx,cy,z0,color);
        quad(p,v,cx,cy,z1,bx,by,z1,ax,ay,z1,ax,ay,z1,color);
        quad(p,v,ax,ay,z0,bx,by,z0,bx,by,z1,ax,ay,z1,color);
        quad(p,v,bx,by,z0,cx,cy,z0,cx,cy,z1,bx,by,z1,color);
        quad(p,v,cx,cy,z0,ax,ay,z0,ax,ay,z1,cx,cy,z1,color);
    }

    private static void quad(PoseStack.Pose p, VertexConsumer v,
                             float ax,float ay,float az,float bx,float by,float bz,
                             float cx,float cy,float cz,float dx,float dy,float dz,int color) {
        float ux=bx-ax,uy=by-ay,uz=bz-az;
        float vx=cx-ax,vy=cy-ay,vz=cz-az;
        float nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;
        float length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
        if(length<.00001f) { nx=0;ny=1;nz=0; } else { nx/=length;ny/=length;nz/=length; }
        vertex(v,p,ax,ay,az,0,0,color,nx,ny,nz);
        vertex(v,p,bx,by,bz,1,0,color,nx,ny,nz);
        vertex(v,p,cx,cy,cz,1,1,color,nx,ny,nz);
        vertex(v,p,dx,dy,dz,0,1,color,nx,ny,nz);
    }

    private static void vertex(VertexConsumer v,PoseStack.Pose p,
                               float x,float y,float z,float u,float vv,int color,
                               float nx,float ny,float nz) {
        v.addVertex(p,x,y,z).setColor(color).setUv(u,vv).setOverlay(0)
                .setLight(LIGHT).setNormal(p,nx,ny,nz);
    }
}
