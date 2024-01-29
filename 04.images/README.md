![5aab35471f97e3db0773b9a72fedc26](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/5aab35471f97e3db0773b9a72fedc26.png)









实时flinksql, 离线doris



smartBI

树云



数据中台

指标平台



信息中心没那么多人, 所以希望数据处理放到应用层, 由业务人员来处理, 但是业务人员不会sql, 所以开发一个工具, 降低业务人员加工数据的门槛







商业版：http://172.16.252.111:30013/login
用户名：admin
密码：dqcf0y>rfpYYi0l     jeecg boot demo 环境更新了密码，请各位知悉





```vue
 <template>
  <a-button @click="getSelection">getSelection</a-button>
  <div class="richText" contenteditable="true" :ref="(el) => (domRef = el)"></div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue';
  const domRef = ref<HTMLDivElement>(null as any);
  const { content = '' } = defineProps<{ content: string }>();
  onMounted(() => {
    domRef.value.innerHTML = content;
  });
  const isCurrentSelection = (node: HTMLElement) => {
    if (!node) {
      return false;
    }
    if (node === domRef.value) {
      return true;
    }
    while ((node = node.parentElement as HTMLDivElement)) {
      if (node === document.body) {
        return false;
      } else if (node === domRef.value) {
        return true;
      }
    }
  };
  const getSelection = () => {
    const selection = window.getSelection();
    const range = new Range();
    const { anchorNode, anchorOffset, focusNode, focusOffset, isCollapsed } = selection as any;
    // 当前光标不在文本框中, 将光标至为最后
    if (!isCurrentSelection(anchorNode) || !isCurrentSelection(focusNode)) {
      const nodes = domRef.value.childNodes || [];
      if (!nodes.length) {
        console.log('光标不在,当前子元素为空');
        range.setStart(domRef.value, 0);
      } else {
        console.log('光标不在,当前子元素不为空');
        range.setStartAfter(nodes[nodes.length - 1]);
      }
    }
    // 光标在文本框中
    else {
      console.log('光标在');
      if (isCollapsed) {
        range.setStart(anchorNode, anchorOffset);
      } else {
        range.setStart(anchorNode, anchorOffset);
        range.setEnd(focusNode, focusOffset);
      }
    }
    return {
      range,
      anchorNode,
      anchorOffset,
      focusNode,
      focusOffset,
      isCollapsed,
    };
  };

  const getContentText = () => {
    return domRef.value.innerText;
  };
  const getContentHtml = () => {
    return domRef.value.innerHTML;
  };
  const setContent = (content: string) => {
    domRef.value.innerHTML = content;
  };
  const _insert = (range: Range, nodes: NodeListOf<ChildNode>) => {
    const positionNode = nodes[nodes.length - 1];
    Array.from(nodes)
      .reverse()
      .forEach((node) => (range as any).insertNode(node));
    // 设置光标位置
    setPositionByNode(positionNode);
  };
  const insertContent = (content: string, { range, anchorNode, anchorOffset, focusNode, focusOffset, isCollapsed } = getSelection()) => {
    const parser = new DOMParser();
    const doc = parser.parseFromString(content, 'text/html');
    const nodes = doc.body.childNodes;
    // 光标选中多个
    if (!isCollapsed) {
      // 先删除
      range.deleteContents();
      const { isCollapsed } = getSelection();
      // 正向多选
      if (isCollapsed) {
        _insert(range, nodes);
      }
      // 反向多选
      else {
        const range = new Range();
        range.setEnd(anchorNode, anchorOffset);
        range.setStart(focusNode, focusOffset);
        range.deleteContents();
        _insert(range, nodes);
      }
    }
    // 光标在具体位置
    else {
      _insert(range, nodes);
    }
    return new Promise((resolve) => {
      focus();
      setTimeout(() => resolve(null));
    });
  };
  const getRichDom = () => {
    return domRef.value;
  };

  const focus = () => {
    domRef.value.focus();
  };
  const setPositionByNode = (positionNode: Node) => {
    const r = document.createRange();
    r.setStartAfter(positionNode);
    r.setEndAfter(positionNode);
    const s = window.getSelection() as Selection;
    s.removeAllRanges();
    s.addRange(r);
  };

  const setPosition = (size = -1) => {
    // 左移
    if (size < 0) {
      _moveLeft(Math.abs(size));
    } else if (size > 0) {
      _moveRight(Math.abs(size));
    } else {
      return 0;
    }
  };

  const _moveLeft = (size: number = 1) => {
    const selection = window.getSelection();
    const { anchorNode, anchorOffset, focusNode, focusOffset, isCollapsed } = selection as any;
    // 当前光标不在文本框中
    if (!isCurrentSelection(anchorNode) || !isCurrentSelection(focusNode)) {
      console.log('当前光标不在文本框中');
      return;
    }
    if (!isCollapsed) {
      console.log('选中多个文字, 非简洁光标');
      return;
    }
    const n = anchorNode.childNodes.item(anchorOffset - 1);
    const str = n.data;
    const r = document.createRange();
    const s = window.getSelection() as Selection;
    r.setStart(n, str.length - size);
    r.setEnd(n, str.length - size);
    s.removeAllRanges();
    s.addRange(r);
  };
  const _moveRight = (size: number) => {
    const selection = window.getSelection();
    const { anchorNode, anchorOffset, focusNode, focusOffset, isCollapsed } = selection as any;
    // 当前光标不在文本框中
    if (!isCurrentSelection(anchorNode) || !isCurrentSelection(focusNode)) {
      console.log('当前光标不在文本框中');
      return;
    }
    if (!isCollapsed) {
      console.log('选中多个文字, 非简洁光标');
      return;
    }
  };

  defineExpose({
    getContentText,
    getContentHtml,
    setContent,
    insertContent,
    getRichDom,
    focus,
    setPosition,
    setPositionByNode,
  });
</script>

<style scoped lang="less">
  .richText {
    border: 1px solid #333;
    border-radius: 5px;
    width: 300px;
    height: 300px;
    padding: 5px;
  }
</style>

```



```shell






docker run --name nginx -p 80:80 -d nginx:1.20




export BASE_DIR=/opt/docker-data/nginx
export NAME=nginx
export VERSION=1.20
docker run --name ${NAME} -p 80:80 -d nginx:${VERSION};
mkdir -p ${BASE_DIR}/logs;
docker cp ${NAME}:/etc/nginx/conf.d/ ${BASE_DIR};
docker cp ${NAME}:/usr/share/nginx/html/ ${BASE_DIR};
docker cp ${NAME}:/etc/nginx/nginx.conf ${BASE_DIR};
docker stop ${NAME};
docker rm ${NAME};
docker run --name nginx -p 80:80 \
-v ${BASE_DIR}/conf.d/:/etc/nginx/conf.d/ \
-v ${BASE_DIR}/nginx.conf:/etc/nginx/nginx.conf \
-v ${BASE_DIR}/html:/usr/share/nginx/html/ \
-v ${BASE_DIR}/logs/:/var/log/nginx/ \
--privileged=true -d nginx:${VERSION};






docker stop nginx;
docker rm nginx;

docker exec -it nginx /bin/bash


# -v /opt/docker-data/nginx/nginx.conf:/etc/nginx/nginx.conf \
# -v /opt/docker-data/nginx/logs/:/var/log/nginx/ \

#-v /opt/docker-data/nginx/conf/:/etc/nginx/conf.d \

docker stop nginx;
docker rm nginx;
```





```shell
#showtooltip
/use [mod]大法师之袍
/use [mod]法力红宝石
/use [mod]法力黄水晶
/use [mod]法力翡翠
/use [mod]法力玛瑙
/castsequence [nocombat] reset=30 制造魔法红宝石,制造魔法黄水晶,制造魔法翡翠,制造魔法玛瑙
/stopmacro [nocombat]
/use 大法师之袍
/use 法力红宝石
/use 法力黄水晶
/use 法力翡翠
/use 法力玛瑙

#showtooltip
/站立
/cast [btn:1]传送：奥格瑞玛
/cast [btn:2]传送门：奥格瑞玛
/stopmacro [btn:1]



/目标 游荡的
/目标 欧莫克大王
/目标 军需官
/目标 卫兵
/目标 血骨傀儡
/目标 霜语
/目标 巴隆
/目标 赫达琳新兵
/目标 赫达琳屠杀者
/目标 德雷克塔尔
/script SetRaidTarget("target",6)
```



15赵琳娜:
vue2: 2年
vue3: 1年
react: 1.5年

认证授权, 不太清楚

vue基础一般

es6还可以

中偏低
