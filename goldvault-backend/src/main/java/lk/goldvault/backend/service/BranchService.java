package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.BranchRequest;
import lk.goldvault.backend.dto.response.BranchResponse;
import lk.goldvault.backend.entity.Branch;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.repository.BranchRepository;
import lk.goldvault.backend.repository.PawnShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final PawnShopRepository pawnShopRepository;

    @Transactional
    public BranchResponse create(Long shopId, BranchRequest request) {
        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));

        if (request.isMain()) {
            demoteExistingMainBranch(shopId);
        }

        Branch branch = Branch.builder()
                .shop(shop)
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .isMain(request.isMain())
                .build();

        branch = branchRepository.save(branch);
        return toResponse(branch);
    }

    public BranchResponse getById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        return toResponse(branch);
    }

    public List<BranchResponse> getByShop(Long shopId) {
        return branchRepository.findByShopId(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));

        if (request.isMain() && !branch.isMain()) {
            demoteExistingMainBranch(branch.getShop().getId());
        }

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setMain(request.isMain());

        branch = branchRepository.save(branch);
        return toResponse(branch);
    }

    public void delete(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));

        if (branch.isMain()) {
            throw new RuntimeException(
                    "Cannot delete the main branch. Designate another branch as main first.");
        }

        branchRepository.delete(branch);
    }

    private void demoteExistingMainBranch(Long shopId) {
        branchRepository.findByShopId(shopId).stream()
                .filter(Branch::isMain)
                .forEach(existing -> {
                    existing.setMain(false);
                    branchRepository.save(existing);
                });
    }

    private BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .shopId(branch.getShop().getId())
                .shopName(branch.getShop().getName())
                .name(branch.getName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .isMain(branch.isMain())
                .createdAt(branch.getCreatedAt())
                .build();
    }
}