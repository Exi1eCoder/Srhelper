package com.quico.srhelper.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.quico.common.annotation.Log;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.enums.BusinessType;
import com.quico.common.utils.SecurityUtils;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.dto.SrCharacterSaveDTO;
import com.quico.srhelper.service.ISrCharacterService;
import com.quico.common.utils.poi.ExcelUtil;
import com.quico.common.core.page.TableDataInfo;

/**
 * 角色Controller
 * 
 * @author quico
 * @date 2026-05-12
 */
@Tag(name = "角色Controller", description = "角色相关接口")
@RestController
@RequestMapping("/srhelper/srCharacter")
public class SrCharacterController extends BaseController
{
    @Autowired
    private ISrCharacterService srCharacterService;

    /**
     * 查询角色列表
     */
    @Operation(summary = "查询角色列表", description = "分页查询角色信息")
    @PreAuthorize("@ss.hasPermi('srhelper:character:list')")
    @GetMapping("/list")
    public TableDataInfo list(SrCharacter srCharacter)
    {
        List<SrCharacter> list = srCharacterService.selectSrCharacterList(srCharacter);
        return getDataTable(list);
    }

    /**
     * 检查角色图鉴修改权限
     */
    @Operation(summary = "检查角色图鉴修改权限", description = "检查当前用户是否有角色图鉴修改权限")
    @GetMapping("/auth/check")
    public AjaxResult checkPermission()
    {
        // 检查用户是否有新增或编辑权限
        boolean hasPermission = SecurityUtils.hasPermi("srhelper:character:add") || SecurityUtils.hasPermi("srhelper:srCharacter:edit");
        return success(hasPermission);
    }

    /**
     * 导出角色列表
     */
    @Operation(summary = "导出角色列表", description = "导出角色列表为Excel文件")
    @PreAuthorize("@ss.hasPermi('srhelper:character:export')")
    @Log(title = "角色", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SrCharacter srCharacter)
    {
        List<SrCharacter> list = srCharacterService.selectSrCharacterList(srCharacter);
        ExcelUtil<SrCharacter> util = new ExcelUtil<SrCharacter>(SrCharacter.class);
        util.exportExcel(response, list, "角色数据");
    }

    /**
     * 导入角色数据（与 export 配套，按角色名字 + 实装版本去重）
     */
    @Operation(summary = "导入角色数据", description = "通过Excel批量导入角色基本信息，updateSupport=true 时已存在记录会被更新")
    @PreAuthorize("@ss.hasPermi('srhelper:character:add') or @ss.hasPermi('srhelper:character:edit')")
    @Log(title = "角色", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SrCharacter> util = new ExcelUtil<SrCharacter>(SrCharacter.class);
        List<SrCharacter> characterList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUserId().toString();
        String message = srCharacterService.importCharacter(characterList, updateSupport, operName);
        return success(message);
    }

    /**
     * 查询角色完整信息（包含所有关联数据）
     */
    @Operation(summary = "查询角色完整信息", description = "根据ID获取角色详细信息，包含晋升材料、技能材料、额外能力、额外属性")
    @PreAuthorize("@ss.hasPermi('srhelper:character:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getDetail(@PathVariable("id") Long id)
    {
        return success(srCharacterService.getDetail(id));
    }

    /**
     * 获取角色基本信息
     */
    @Operation(summary = "获取角色基本信息", description = "根据ID获取角色基本信息")
    @PreAuthorize("@ss.hasPermi('srhelper:character:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(srCharacterService.selectSrCharacterById(id));
    }

    /**
     * 统一保存/更新角色（包含所有关联数据）
     */
    @Operation(summary = "统一保存/更新角色", description = "保存或更新角色及其所有晋升材料配置（一次保存四张表）")
    @PreAuthorize("@ss.hasPermi('srhelper:character:add') or @ss.hasPermi('srhelper:character:edit')")
    @Log(title = "角色", businessType = BusinessType.INSERT)
    @PostMapping("/saveAll")
    public AjaxResult saveAll(@RequestBody SrCharacterSaveDTO dto)
    {
        Long characterId = srCharacterService.saveAll(dto);
        return success(characterId);
    }

    /**
     * 新增角色（基本信息）
     */
    @Operation(summary = "新增角色基本信息", description = "仅新增角色基本信息")
    @PreAuthorize("@ss.hasPermi('srhelper:character:add')")
    @Log(title = "角色", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SrCharacter srCharacter)
    {
        return toAjax(srCharacterService.insertSrCharacter(srCharacter));
    }

    /**
     * 修改角色（基本信息）
     */
    @Operation(summary = "修改角色基本信息", description = "仅更新角色基本信息")
    @PreAuthorize("@ss.hasPermi('srhelper:character:edit')")
    @Log(title = "角色", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SrCharacter srCharacter)
    {
        return toAjax(srCharacterService.updateSrCharacter(srCharacter));
    }

    /**
     * 删除角色（包含所有关联数据）
     */
    @Operation(summary = "删除角色", description = "删除角色及其所有材料配置")
    @PreAuthorize("@ss.hasPermi('srhelper:character:remove')")
    @Log(title = "角色", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        srCharacterService.deleteCharacter(id);
        return success();
    }

    /**
     * 批量删除角色（基本信息）
     */
    @Operation(summary = "批量删除角色", description = "仅删除角色基本信息")
    @PreAuthorize("@ss.hasPermi('srhelper:character:remove')")
    @Log(title = "角色", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch/{ids}")
    public AjaxResult removeBatch(@PathVariable Long[] ids)
    {
        return toAjax(srCharacterService.deleteSrCharacterByIds(ids));
    }
}