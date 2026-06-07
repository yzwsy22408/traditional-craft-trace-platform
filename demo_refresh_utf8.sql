SET NAMES utf8mb4;
USE craft_trace;

UPDATE student_work SET
  work_title = '花窗纹样木雕挂饰',
  image_url = '/images/demo-works/woodcarving_1.png',
  reflection = '第一次完整体验木雕起稿、浅雕和打磨的流程，完成后对刀具发力和转折处理更有感觉。',
  gain_text = '课堂上学会了顺着木纹下刀，后续想继续尝试更细致的浮雕纹样。'
WHERE course_id = 1 AND student_user_id = 1;

UPDATE student_work SET
  work_title = '古典回纹木雕书签',
  image_url = '/images/demo-works/woodcarving_2.png',
  reflection = '在老师指导下完成了回纹边框和流苏装饰，整体比例比预想中更协调。',
  gain_text = '掌握了基础木雕的起线和收刀方法，后续愿意继续练习边角细节。'
WHERE course_id = 1 AND student_user_id = 2;

UPDATE student_work SET
  work_title = '壮锦纹样编织杯垫',
  image_url = '/images/demo-works/zhuangjin_1.png',
  reflection = '课堂练习后把传统纹样整理成杯垫图案，色彩层次比第一次尝试更稳定。',
  gain_text = '理解了经纬组织与图案重复规律，配色也比之前更统一。'
WHERE course_id = 2 AND student_user_id = 2;

UPDATE student_work SET
  work_title = '壮锦风收纳小包',
  image_url = '/images/demo-works/zhuangjin_2.png',
  reflection = '把课堂学到的基础纹样做进了收纳小包，边缘收口和图案密度控制得更自然。',
  gain_text = '对壮锦纹样节奏和织造层次有了更直观的认识。'
WHERE course_id = 2 AND student_user_id = 3;

UPDATE student_work SET
  work_title = '青花莲纹练习盘',
  image_url = '/images/demo-works/qinghua_1.png',
  reflection = '从勾线到上色再到整体布局都很顺畅，最后成品比预想更完整。',
  gain_text = '练习了青花常见的线描和晕染，构图意识明显增强。'
WHERE course_id = 3 AND student_user_id = 1;

UPDATE student_work SET
  work_title = '青花山水纹小杯',
  image_url = '/images/demo-works/qinghua_2.png',
  reflection = '尝试把山水元素压缩到小器型上，细节虽简化但整体意境保留得不错。',
  gain_text = '掌握了留白与纹样密度控制的方法，课堂收获很大。'
WHERE course_id = 3 AND student_user_id = 2;

UPDATE student_work SET
  work_title = '双面绣团扇练习',
  image_url = '/images/demo-works/suxiu_1.png',
  reflection = '正反两面的颜色衔接比之前更自然，针脚也更细密。',
  gain_text = '进一步理解了分丝、劈丝和双面绣的过渡处理。'
WHERE course_id = 4 AND student_user_id = 2;

UPDATE student_work SET
  work_title = '花鸟主题绣片',
  image_url = '/images/demo-works/suxiu_2.png',
  reflection = '课堂上完成了花鸟绣片的主体部分，羽毛层次和花瓣颜色过渡都比较满意。',
  gain_text = '对苏绣针法的节奏感和配色关系有了更深理解。'
WHERE course_id = 4 AND student_user_id = 3;

UPDATE student_work SET
  work_title = '苗银花纹吊坠',
  image_url = '/images/demo-works/miaoyin_1.png',
  reflection = '把课堂上的敲錾练习整理成吊坠造型，纹样边缘比第一次尝试更清晰。',
  gain_text = '学会了基础纹样敲制和表面修整，成品完成度很高。'
WHERE course_id = 5 AND student_user_id = 1;

UPDATE student_work SET
  work_title = '银饰卷草纹胸针',
  image_url = '/images/demo-works/miaoyin_2.png',
  reflection = '尝试把卷草纹和苗族纹样结合在小胸针上，效果比预想更精致。',
  gain_text = '进一步熟悉了錾刻的力度控制，细线条处理更稳。'
WHERE course_id = 5 AND student_user_id = 3;

UPDATE student_work SET
  work_title = '脱胎漆器小圆盒',
  image_url = '/images/demo-works/lacquer_1.png',
  reflection = '完成脱胎、上灰和打磨后的整体效果很细腻，漆面光泽也比较均匀。',
  gain_text = '第一次完整了解脱胎漆器流程，对福州传统漆艺印象非常深。'
WHERE course_id = 6 AND student_user_id = 1;

UPDATE student_work SET
  work_title = '课堂漆艺笔筒',
  image_url = '/images/demo-works/lacquer_2.png',
  reflection = '课堂上重点练习了边缘修整和抛光，成品表面层次更丰富。',
  gain_text = '学会了脱胎漆器成型与后期打磨的关键步骤，体验很好。'
WHERE course_id = 6 AND student_user_id = 2;

UPDATE student_work SET
  work_title = '紫砂随形小壶',
  image_url = '/images/demo-works/zisha_1.png',
  reflection = '从拍泥到壶身成型的过程很有趣，最后的小壶比例和壶嘴衔接都比较自然。',
  gain_text = '对紫砂泥性和成型步骤有了完整认识，动手体验很扎实。'
WHERE course_id = 7 AND student_user_id = 1;

UPDATE student_work SET
  work_title = '紫砂茶仓练习器',
  image_url = '/images/demo-works/zisha_2.png',
  reflection = '课堂上重点练习了器型收口和表面修整，整体造型更协调。',
  gain_text = '学到了紫砂成型和烧制前检查的关键点，成品很适合展示。'
WHERE course_id = 7 AND student_user_id = 2;

UPDATE course_review SET
  course_name = '传统木雕基础体验课',
  content = '老师会先示范刀具的握法和下刀方向，再让大家一步一步完成自己的木雕作品，整个体验很沉浸。',
  reply = '你的木纹处理很自然，后续可以继续尝试更细的阴刻纹样。'
WHERE course_id = 1 AND student_user_id = 1;

UPDATE course_review SET
  course_name = '传统木雕基础体验课',
  content = '课堂氛围很好，老师会及时纠正握刀姿势，作品从粗坯到细修的变化特别明显，适合第一次接触木雕的同学。',
  reply = '完成度很高，回纹边框已经有不错的节奏感了。'
WHERE course_id = 1 AND student_user_id = 2;

UPDATE course_review SET
  course_name = '传统木雕基础体验课',
  content = '课程安排比较清晰，既能看示范也能亲手操作，成品虽然简单，但成就感很足。',
  reply = '继续保持，多练几次细节会更稳定。'
WHERE course_id = 1 AND student_user_id = 3;

UPDATE course_review SET
  course_name = '广西壮锦技艺初探',
  content = '壮锦课程把纹样来源、配色规律和上机体验结合得很好，既能了解文化背景，也能感受到传统织造的精细。',
  reply = '配色思路很好，后续可以继续强化主体纹样的对称关系。'
WHERE course_id = 2 AND student_user_id = 2;

UPDATE course_review SET
  course_name = '广西壮锦技艺初探',
  content = '课程内容很丰富，老师讲解传统纹样时很细致，亲手完成一段基础织样之后，对壮锦有了更直观的认识。',
  reply = '完成得不错，课堂上的基础织样节奏已经很稳定了。'
WHERE course_id = 2 AND student_user_id = 3;

UPDATE course_review SET
  course_name = '青花瓷手绘体验营',
  content = '从构图到上色都很有参与感，老师会一步一步示范青花线描，最后看到成品很惊喜。',
  reply = '瓷盘的线条很流畅，后续可以继续尝试更复杂的边饰。'
WHERE course_id = 3 AND student_user_id = 1;

UPDATE course_review SET
  course_name = '青花瓷手绘体验营',
  content = '课堂体验非常完整，既介绍了青花瓷的历史，也安排了足够的动手时间，适合拍照展示和成果留存。',
  reply = '小杯构图很完整，山水留白处理得不错。'
WHERE course_id = 3 AND student_user_id = 2;

UPDATE course_review SET
  course_name = '苏绣双面绣精研班',
  content = '苏绣课程比想象中更细致，老师对针法、分丝和配色的讲解都很清楚，成品效果很精美。',
  reply = '针脚已经很细密了，继续练习会更稳定。'
WHERE course_id = 4 AND student_user_id = 2;

UPDATE course_review SET
  course_name = '苏绣双面绣精研班',
  content = '双面绣确实有难度，但课程安排循序渐进，完成一个小型绣片之后成就感特别强。',
  reply = '整体层次感不错，后续可以多练习羽毛和花瓣的过渡。'
WHERE course_id = 4 AND student_user_id = 3;

UPDATE course_review SET
  course_name = '苗族银饰錾刻课程',
  content = '银饰錾刻课程很有特色，老师会先示范再让大家上手，敲击节奏和纹样成型过程都很解压。',
  reply = '吊坠的中心纹样处理得很完整，继续保持。'
WHERE course_id = 5 AND student_user_id = 1;

UPDATE course_review SET
  course_name = '苗族银饰錾刻课程',
  content = '课堂动手环节很多，既能了解苗银工艺，也能自己完成纹样敲制，展示效果很好。',
  reply = '胸针构图挺有想法，细线条控制也越来越稳。'
WHERE course_id = 5 AND student_user_id = 3;

UPDATE course_review SET
  course_name = '脱胎漆器研学课',
  content = '漆器课程非常系统，从胎体理解到后续打磨都安排得很完整，课堂展示和讲解都很到位。',
  reply = '小圆盒的表面处理很细腻，继续保持这个完成度。'
WHERE course_id = 6 AND student_user_id = 1;

UPDATE course_review SET
  course_name = '脱胎漆器研学课',
  content = '老师和匠人配合得很好，漆艺制作流程讲解特别清楚，成品很适合放在成果展示区域。',
  reply = '笔筒的边缘修整得很干净，整体观感很好。'
WHERE course_id = 6 AND student_user_id = 2;

UPDATE course_review SET
  course_name = '脱胎漆器研学课',
  content = '课程内容很完整，老师带队清晰，匠人讲解细致，实际动手体验很强，能够清楚了解脱胎漆器的制作流程，收获很大。',
  reply = NULL
WHERE course_id = 6 AND student_user_id = 11;

UPDATE course_review SET
  course_name = '紫砂手工壶全流程',
  content = '紫砂课程把泥料处理、拍泥成型和后续修整讲得很完整，做出来的作品也很有成就感。',
  reply = '壶身比例控制得不错，继续练习会更成熟。'
WHERE course_id = 7 AND student_user_id = 1;

UPDATE course_review SET
  course_name = '紫砂手工壶全流程',
  content = '整门课既有文化介绍也有大量实操，尤其是装把和细修部分特别有意思，适合答辩展示。',
  reply = '课堂完成度很高，成型和细节处理都很稳定。'
WHERE course_id = 7 AND student_user_id = 2;

UPDATE course SET
  intro = '课程以传统木雕入门为主线，围绕识木、起稿、浅雕、打磨四个环节展开。学生可在匠人与带队教师的指导下，完成一件具有传统纹样元素的小型木雕作品，感受木作工艺从构想到成型的全过程。',
  lead_route = '在校内集合签到后前往木雕研学工坊，依次开展安全讲解、木材与工具认知、纹样起稿、分组雕刻、细节打磨、作品展示与课程总结。'
WHERE id = 1;

UPDATE course SET
  intro = '课程围绕广西壮锦的色彩体系与基础织造技法展开，帮助学生理解民族纹样背后的文化寓意。课堂设置经纬练习、图案配色和小件成品制作环节，适合用于展示研学成果与非遗体验过程。',
  lead_route = '在校内集合签到后前往壮锦研学工坊，依次进行文化导入、纹样观察、织造示范、分组编织、作品整理、成果分享与课程总结。'
WHERE id = 2;

UPDATE course SET
  intro = '课程以青花瓷基础纹样描绘为核心，带领学生认识青花颜料、常见器型和基础构图方法。学生可在课堂中完成小型瓷盘或器皿的青花绘制练习，体验传统陶瓷装饰工艺的审美与节奏。',
  lead_route = '在校内集合签到后前往青花瓷体验教室，依次完成青花历史导入、线描示范、分组绘制、细节补色、作品展示与课堂总结。'
WHERE id = 3;

UPDATE course SET
  intro = '课程聚焦苏绣的基础针法、分丝技巧与色彩过渡方法，通过小型绣片与团扇练习帮助学生理解传统刺绣的细腻表达。课堂兼顾文化介绍与动手体验，适合答辩时展示学生作品成果。',
  lead_route = '在校内集合签到后前往苏绣研修工坊，依次开展针法讲解、分丝示范、图样练习、分组刺绣、作品整理与课程总结。'
WHERE id = 4;

UPDATE course SET
  intro = '课程从苗族银饰的典型纹样与基础錾刻方法切入，帮助学生认识非遗银饰工艺的制作流程。课堂安排纹样描摹、敲錾练习和小件成品制作，学生可完成吊坠、胸针等简易课堂成果。',
  lead_route = '在校内集合签到后前往苗银体验工坊，依次完成工艺导入、工具说明、纹样描摹、分组敲錾、作品修整与课堂总结。'
WHERE id = 5;

UPDATE course SET
  intro = '课程围绕脱胎漆器的胎体制作、表面修整与漆层表现展开，帮助学生理解传统漆艺从结构到质感的制作逻辑。课堂成果以小圆盒、笔筒等易展示作品为主，适合呈现研学课程的实践价值。',
  lead_route = '在校内集合签到后前往漆器研学工坊，依次进行漆艺导入、胎体讲解、表面处理示范、分组制作、作品展示与课程总结。'
WHERE id = 6;

UPDATE course SET
  intro = '课程以紫砂泥料认知、器型塑造与基础成型方法为核心，带领学生体验紫砂手工壶制作的主要步骤。课堂更强调动手参与和器型观察，学生可完成小壶或茶仓练习器等入门级成果作品。',
  lead_route = '在校内集合签到后前往紫砂研学教室，依次开展泥料认知、拍泥练习、器型塑造、细节修整、作品展示与课程总结。'
WHERE id = 7;
